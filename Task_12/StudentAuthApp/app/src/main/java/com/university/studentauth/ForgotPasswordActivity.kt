package com.university.studentauth

import android.os.Bundle
import android.util.Patterns
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.university.studentauth.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Reset Password"

        auth = FirebaseAuth.getInstance()

        binding.btnSendReset.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()

            if (email.isEmpty()) {
                binding.etEmail.error = "Email is required"
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmail.error = "Invalid email"
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            binding.btnSendReset.isEnabled = false

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    binding.progressBar.visibility = View.GONE
                    binding.btnSendReset.isEnabled = true
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Reset email sent. Check your inbox.", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        val errorMsg = task.exception?.message ?: "Failed to send reset email"
                        Snackbar.make(binding.root, errorMsg, Snackbar.LENGTH_LONG).show()
                    }
                }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
