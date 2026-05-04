package com.university.usersettings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private lateinit var appSettingsPrefs: SharedPreferences
    private lateinit var profilePrefs: SharedPreferences

    private lateinit var etStudentName: EditText
    private lateinit var rgTheme: RadioGroup
    private lateinit var rbLight: RadioButton
    private lateinit var rbDark: RadioButton
    private lateinit var rbSystem: RadioButton
    private lateinit var swNotifications: SwitchCompat
    private lateinit var spLanguage: Spinner
    private lateinit var seekFontSize: SeekBar
    private lateinit var tvFontSizeValue: TextView

    private lateinit var languageOptions: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        appSettingsPrefs = getSharedPreferences(Constants.PREF_APP_SETTINGS, MODE_PRIVATE)
        applyThemeMode(appSettingsPrefs.getString(Constants.KEY_THEME, "light").orEmpty())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        profilePrefs = getSharedPreferences(Constants.PREF_PROFILE, MODE_PRIVATE)

        bindViews()
        setupLanguageSpinner()
        setupSeekBar()
        setupActions()
    }

    override fun onResume() {
        super.onResume()
        applyThemeMode(appSettingsPrefs.getString(Constants.KEY_THEME, "light").orEmpty())
        restoreUiFromPrefs()
    }

    private fun bindViews() {
        etStudentName = findViewById(R.id.etStudentName)
        rgTheme = findViewById(R.id.rgTheme)
        rbLight = findViewById(R.id.rbThemeLight)
        rbDark = findViewById(R.id.rbThemeDark)
        rbSystem = findViewById(R.id.rbThemeSystem)
        swNotifications = findViewById(R.id.swNotifications)
        spLanguage = findViewById(R.id.spLanguage)
        seekFontSize = findViewById(R.id.seekFontSize)
        tvFontSizeValue = findViewById(R.id.tvFontSizeValue)
    }

    private fun setupLanguageSpinner() {
        languageOptions = resources.getStringArray(R.array.languages_array).toList()
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            languageOptions
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spLanguage.adapter = adapter
    }

    private fun setupSeekBar() {
        seekFontSize.max = 12
        seekFontSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val fontSize = progress + 12
                tvFontSizeValue.text = getString(R.string.font_size_format, fontSize)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
    }

    private fun setupActions() {
        findViewById<Button>(R.id.btnSaveSettings).setOnClickListener {
            saveSettings()
        }
        findViewById<Button>(R.id.btnResetDefaults).setOnClickListener {
            resetToDefaults()
        }
        findViewById<Button>(R.id.btnViewSavedSettings).setOnClickListener {
            startActivity(Intent(this, SettingsViewerActivity::class.java))
        }
        findViewById<FloatingActionButton>(R.id.fabProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun saveSettings() {
        val selectedTheme = when (rgTheme.checkedRadioButtonId) {
            R.id.rbThemeDark -> "dark"
            R.id.rbThemeSystem -> "system"
            else -> "light"
        }

        val studentName = etStudentName.text.toString().trim()
        val language = spLanguage.selectedItem?.toString() ?: "English"
        val fontSize = seekFontSize.progress + 12

        appSettingsPrefs.edit()
            .putString(Constants.KEY_THEME, selectedTheme)
            .putBoolean(Constants.KEY_NOTIFICATIONS, swNotifications.isChecked)
            .putString(Constants.KEY_LANGUAGE, language)
            .putInt(Constants.KEY_FONT_SIZE, fontSize)
            .putLong(Constants.KEY_LAST_SAVED, System.currentTimeMillis())
            .apply()

        profilePrefs.edit()
            .putString(Constants.KEY_STUDENT_NAME, studentName)
            .apply()

        applyThemeMode(selectedTheme)
        Toast.makeText(this, getString(R.string.toast_settings_saved), Toast.LENGTH_SHORT).show()
    }

    private fun resetToDefaults() {
        appSettingsPrefs.edit().clear().apply()

        rbLight.isChecked = true
        swNotifications.isChecked = true
        spLanguage.setSelection(0)
        seekFontSize.progress = 4
        tvFontSizeValue.text = getString(R.string.font_size_format, 16)

        applyThemeMode("light")
        Toast.makeText(this, getString(R.string.toast_reset_done), Toast.LENGTH_SHORT).show()
    }

    private fun applyThemeMode(theme: String) {
        val mode = when (theme) {
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            "system" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else -> AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun restoreUiFromPrefs() {
        val savedStudentName = profilePrefs.getString(Constants.KEY_STUDENT_NAME, "").orEmpty()
        etStudentName.setText(savedStudentName)

        when (appSettingsPrefs.getString(Constants.KEY_THEME, "light")) {
            "dark" -> rbDark.isChecked = true
            "system" -> rbSystem.isChecked = true
            else -> rbLight.isChecked = true
        }

        swNotifications.isChecked = appSettingsPrefs.getBoolean(Constants.KEY_NOTIFICATIONS, true)

        val savedLanguage = appSettingsPrefs.getString(Constants.KEY_LANGUAGE, "English").orEmpty()
        val languageIndex = languageOptions.indexOf(savedLanguage).takeIf { it >= 0 } ?: 0
        spLanguage.setSelection(languageIndex)

        val savedFont = appSettingsPrefs.getInt(Constants.KEY_FONT_SIZE, 16).coerceIn(12, 24)
        seekFontSize.progress = savedFont - 12
        tvFontSizeValue.text = getString(R.string.font_size_format, savedFont)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}