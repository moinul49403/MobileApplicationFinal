package com.university.usersettings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsViewerActivity : AppCompatActivity() {
    private lateinit var tvNoData: TextView
    private lateinit var tvLastSaved: TextView

    private lateinit var cardTheme: View
    private lateinit var cardNotifications: View
    private lateinit var cardLanguage: View
    private lateinit var cardFontSize: View
    private lateinit var cardStudentName: View
    private lateinit var cardStudentId: View
    private lateinit var cardDepartment: View
    private lateinit var cardYear: View
    private lateinit var cardEmail: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_viewer)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bindViews()
        setupButtons()
        renderSavedSettings()
    }

    private fun bindViews() {
        tvNoData = findViewById(R.id.tvNoData)
        tvLastSaved = findViewById(R.id.tvLastSaved)

        cardTheme = findViewById(R.id.cardTheme)
        cardNotifications = findViewById(R.id.cardNotifications)
        cardLanguage = findViewById(R.id.cardLanguage)
        cardFontSize = findViewById(R.id.cardFontSize)
        cardStudentName = findViewById(R.id.cardStudentName)
        cardStudentId = findViewById(R.id.cardStudentId)
        cardDepartment = findViewById(R.id.cardDepartment)
        cardYear = findViewById(R.id.cardYear)
        cardEmail = findViewById(R.id.cardEmail)
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnEdit).setOnClickListener {
            finish()
        }
    }

    private fun renderSavedSettings() {
        val appPrefs = getSharedPreferences(Constants.PREF_APP_SETTINGS, MODE_PRIVATE)
        val profilePrefs = getSharedPreferences(Constants.PREF_PROFILE, MODE_PRIVATE)
        val lastSaved = appPrefs.getLong(Constants.KEY_LAST_SAVED, 0L)

        if (lastSaved == 0L) {
            tvNoData.visibility = View.VISIBLE
            tvLastSaved.visibility = View.GONE
            setCardsVisibility(View.GONE)
            return
        }

        tvNoData.visibility = View.GONE
        tvLastSaved.visibility = View.VISIBLE
        setCardsVisibility(View.VISIBLE)

        val theme = when (appPrefs.getString(Constants.KEY_THEME, "light")) {
            "dark" -> "Dark"
            "system" -> "System Default"
            else -> "Light"
        }
        val notifications = if (appPrefs.getBoolean(Constants.KEY_NOTIFICATIONS, true)) "Enabled" else "Disabled"
        val language = appPrefs.getString(Constants.KEY_LANGUAGE, "English").orEmpty()
        val fontSize = appPrefs.getInt(Constants.KEY_FONT_SIZE, 16)
        val studentName = profilePrefs.getString(Constants.KEY_STUDENT_NAME, "").orEmpty()
        val studentId = profilePrefs.getString(Constants.KEY_STUDENT_ID, "").orEmpty()
        val department = profilePrefs.getString(Constants.KEY_DEPARTMENT, "").orEmpty()
        val year = profilePrefs.getString(Constants.KEY_YEAR, "").orEmpty()
        val email = profilePrefs.getString(Constants.KEY_EMAIL, "").orEmpty()

        findViewById<TextView>(R.id.tvThemeValue).text = theme
        findViewById<TextView>(R.id.tvNotificationsValue).text = notifications
        findViewById<TextView>(R.id.tvLanguageValue).text = language
        findViewById<TextView>(R.id.tvFontSizeValue).text = "$fontSize sp"
        findViewById<TextView>(R.id.tvStudentNameValue).text = studentName.ifBlank { "-" }
        findViewById<TextView>(R.id.tvStudentIdValue).text = studentId.ifBlank { "-" }
        findViewById<TextView>(R.id.tvDepartmentValue).text = department.ifBlank { "-" }
        findViewById<TextView>(R.id.tvYearValue).text = year.ifBlank { "-" }
        findViewById<TextView>(R.id.tvEmailValue).text = email.ifBlank { "-" }

        val format = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateText = format.format(Date(lastSaved))
        tvLastSaved.text = getString(R.string.last_saved_format, dateText)
    }

    private fun setCardsVisibility(visibility: Int) {
        cardTheme.visibility = visibility
        cardNotifications.visibility = visibility
        cardLanguage.visibility = visibility
        cardFontSize.visibility = visibility
        cardStudentName.visibility = visibility
        cardStudentId.visibility = visibility
        cardDepartment.visibility = visibility
        cardYear.visibility = visibility
        cardEmail.visibility = visibility
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
