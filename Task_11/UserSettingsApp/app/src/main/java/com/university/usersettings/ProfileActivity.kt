package com.university.usersettings

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {
    private lateinit var tvWelcomeBanner: TextView
    private lateinit var etStudentId: EditText
    private lateinit var etFullName: EditText
    private lateinit var spDepartment: Spinner
    private lateinit var spYear: Spinner
    private lateinit var etEmail: EditText

    private lateinit var departments: List<String>
    private lateinit var years: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bindViews()
        setupSpinners()
        setupSaveButton()
    }

    override fun onResume() {
        super.onResume()
        restoreProfile()
    }

    private fun bindViews() {
        tvWelcomeBanner = findViewById(R.id.tvWelcomeBanner)
        etStudentId = findViewById(R.id.etStudentId)
        etFullName = findViewById(R.id.etFullName)
        spDepartment = findViewById(R.id.spDepartment)
        spYear = findViewById(R.id.spYear)
        etEmail = findViewById(R.id.etEmail)
    }

    private fun setupSpinners() {
        departments = resources.getStringArray(R.array.departments_array).toList()
        years = resources.getStringArray(R.array.years_array).toList()

        val departmentAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            departments
        )
        departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spDepartment.adapter = departmentAdapter

        val yearAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            years
        )
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spYear.adapter = yearAdapter
    }

    private fun setupSaveButton() {
        findViewById<Button>(R.id.btnSaveProfile).setOnClickListener {
            val profilePrefs = getSharedPreferences(Constants.PREF_PROFILE, MODE_PRIVATE)
            profilePrefs.edit()
                .putString(Constants.KEY_STUDENT_NAME, etFullName.text.toString().trim())
                .putString(Constants.KEY_STUDENT_ID, etStudentId.text.toString().trim())
                .putString(Constants.KEY_DEPARTMENT, spDepartment.selectedItem.toString())
                .putString(Constants.KEY_YEAR, spYear.selectedItem.toString())
                .putString(Constants.KEY_EMAIL, etEmail.text.toString().trim())
                .apply()

            updateWelcomeBanner(etFullName.text.toString().trim())
            Toast.makeText(this, getString(R.string.toast_profile_saved), Toast.LENGTH_SHORT).show()
        }
    }

    private fun restoreProfile() {
        val profilePrefs = getSharedPreferences(Constants.PREF_PROFILE, MODE_PRIVATE)
        val name = profilePrefs.getString(Constants.KEY_STUDENT_NAME, "").orEmpty()
        val studentId = profilePrefs.getString(Constants.KEY_STUDENT_ID, "").orEmpty()
        val department = profilePrefs.getString(Constants.KEY_DEPARTMENT, departments.first()).orEmpty()
        val year = profilePrefs.getString(Constants.KEY_YEAR, years.first()).orEmpty()
        val email = profilePrefs.getString(Constants.KEY_EMAIL, "").orEmpty()

        etFullName.setText(name)
        etStudentId.setText(studentId)
        etEmail.setText(email)

        spDepartment.setSelection(departments.indexOf(department).takeIf { it >= 0 } ?: 0)
        spYear.setSelection(years.indexOf(year).takeIf { it >= 0 } ?: 0)

        updateWelcomeBanner(name)
    }

    private fun updateWelcomeBanner(name: String) {
        val displayName = if (name.isBlank()) getString(R.string.default_student_name) else name
        tvWelcomeBanner.text = getString(R.string.welcome_banner_format, displayName)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
