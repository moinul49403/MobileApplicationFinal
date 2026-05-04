package com.university.studentauth

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.university.studentauth.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()

        val loginText = binding.tvLoginLink.text.toString()
        val loginStart = loginText.indexOf("Login")
        if (loginStart >= 0) {
            val spannable = SpannableString(loginText)
            val loginEnd = loginStart + "Login".length
            spannable.setSpan(UnderlineSpan(), loginStart, loginEnd, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(
                StyleSpan(android.graphics.Typeface.BOLD),
                loginStart,
                loginEnd,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            binding.tvLoginLink.text = spannable
        }

        binding.tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btnRegister.setOnClickListener {
            validateAndRegister()
        }
    }

    private fun validateAndRegister() {
        val name = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (name.isEmpty()) {
            binding.etFullName.error = "Full name is required"
            return
        }
        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Invalid email format"
            return
        }
        if (password.isEmpty()) {
            binding.etPassword.error = "Password is required"
            return
        }
        if (password.length < 8) {
            binding.etPassword.error = "Password must be at least 8 characters"
            return
        }
        if (confirmPassword != password) {
            binding.etConfirmPassword.error = "Passwords do not match"
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                binding.progressBar.visibility = View.GONE
                binding.btnRegister.isEnabled = true
                if (task.isSuccessful) {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    val errorMsg = task.exception?.message ?: "Registration failed"
                    Snackbar.make(binding.root, errorMsg, Snackbar.LENGTH_LONG).show()
                }
            }
    }
}
