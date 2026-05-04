package com.university.studentauth

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.university.studentauth.databinding.ActivityHomeBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        if (user == null) {
            navigateToLogin()
            return
        }

        binding.tvEmail.text = user.email ?: getString(R.string.default_no_email)
        binding.tvWelcomeEmail.text = user.email ?: ""
        binding.tvAvatarInitial.text = user.email?.get(0)?.uppercaseChar()?.toString() ?: getString(R.string.default_avatar_initial)
        binding.tvUid.text = "${user.uid.take(8)}..."

        val createdDate = user.metadata?.creationTimestamp ?: 0L
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        binding.tvCreatedDate.text = sdf.format(Date(createdDate))

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            navigateToLogin()
        }

        binding.btnUpdatePassword.setOnClickListener {
            val newPass = binding.etNewPassword.text.toString()
            val confirmPass = binding.etConfirmNewPassword.text.toString()

            if (newPass.length < 8) {
                binding.etNewPassword.error = "Min 8 characters"
                return@setOnClickListener
            }
            if (newPass != confirmPass) {
                binding.etConfirmNewPassword.error = "Passwords do not match"
                return@setOnClickListener
            }

            binding.progressBarPassword.visibility = View.VISIBLE
            binding.btnUpdatePassword.isEnabled = false

            user.updatePassword(newPass)
                .addOnCompleteListener { task ->
                    binding.progressBarPassword.visibility = View.GONE
                    binding.btnUpdatePassword.isEnabled = true
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                        binding.etNewPassword.setText("")
                        binding.etConfirmNewPassword.setText("")
                    } else {
                        Snackbar.make(binding.root, task.exception?.message ?: "Update failed", Snackbar.LENGTH_LONG).show()
                    }
                }
        }

        binding.btnDeleteAccount.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to permanently delete your account? This action cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    user.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Account deleted.", Toast.LENGTH_SHORT).show()
                                navigateToLogin()
                            } else {
                                Snackbar.make(binding.root, task.exception?.message ?: "Delete failed", Snackbar.LENGTH_LONG).show()
                            }
                        }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
