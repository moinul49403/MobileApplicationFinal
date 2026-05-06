package com.university.studentcoursemanager

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AddCourseActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tilCourseName: TextInputLayout
    private lateinit var tilCourseCode: TextInputLayout
    private lateinit var tilInstructor: TextInputLayout
    private lateinit var tilSchedule: TextInputLayout
    private lateinit var tilRoom: TextInputLayout
    private lateinit var inputCourseName: TextInputEditText
    private lateinit var inputCourseCode: TextInputEditText
    private lateinit var inputInstructor: TextInputEditText
    private lateinit var inputSchedule: TextInputEditText
    private lateinit var inputRoom: TextInputEditText
    private lateinit var pickerCreditHours: NumberPicker
    private lateinit var spinnerSemester: Spinner
    private lateinit var progressSave: LinearProgressIndicator
    private lateinit var buttonSaveCourse: MaterialButton
    private lateinit var buttonCancel: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_course)

        toolbar = findViewById(R.id.toolbarAdd)
        tilCourseName = findViewById(R.id.tilCourseName)
        tilCourseCode = findViewById(R.id.tilCourseCode)
        tilInstructor = findViewById(R.id.tilInstructor)
        tilSchedule = findViewById(R.id.tilSchedule)
        tilRoom = findViewById(R.id.tilRoom)
        inputCourseName = findViewById(R.id.inputCourseName)
        inputCourseCode = findViewById(R.id.inputCourseCode)
        inputInstructor = findViewById(R.id.inputInstructor)
        inputSchedule = findViewById(R.id.inputSchedule)
        inputRoom = findViewById(R.id.inputRoom)
        pickerCreditHours = findViewById(R.id.pickerCreditHours)
        spinnerSemester = findViewById(R.id.spinnerSemester)
        progressSave = findViewById(R.id.progressSave)
        buttonSaveCourse = findViewById(R.id.buttonSaveCourse)
        buttonCancel = findViewById(R.id.buttonCancel)

        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        pickerCreditHours.minValue = 1
        pickerCreditHours.maxValue = 4
        pickerCreditHours.value = 3

        ArrayAdapter.createFromResource(
            this,
            R.array.semester_options,
            android.R.layout.simple_spinner_item,
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerSemester.adapter = adapter
        }

        buttonCancel.setOnClickListener { finish() }

        buttonSaveCourse.setOnClickListener { attemptSave() }
    }

    private fun attemptSave() {
        if (!validateRequiredFields()) return

        val ref = FirebaseCourses.ref(this).push()
        val key = ref.key
        if (key.isNullOrEmpty()) {
            Toast.makeText(this, R.string.error_generate_course_id, Toast.LENGTH_SHORT).show()
            return
        }

        val course = Course(
            id = key,
            name = inputCourseName.text?.toString()?.trim().orEmpty(),
            code = inputCourseCode.text?.toString()?.trim().orEmpty(),
            instructor = inputInstructor.text?.toString()?.trim().orEmpty(),
            creditHours = pickerCreditHours.value,
            schedule = inputSchedule.text?.toString()?.trim().orEmpty(),
            room = inputRoom.text?.toString()?.trim().orEmpty(),
            semester = spinnerSemester.selectedItem?.toString().orEmpty(),
        )

        setSavingUi(true)
        ref.setValue(course)
            .addOnCompleteListener { task ->
                setSavingUi(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, R.string.toast_course_added, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        task.exception?.localizedMessage ?: getString(R.string.error_save_failed),
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
    }

    private fun validateRequiredFields(): Boolean {
        var valid = true
        if (inputCourseName.text.isNullOrBlank()) {
            tilCourseName.error = getString(R.string.error_required_field)
            valid = false
        } else {
            tilCourseName.error = null
        }
        if (inputCourseCode.text.isNullOrBlank()) {
            tilCourseCode.error = getString(R.string.error_required_field)
            valid = false
        } else {
            tilCourseCode.error = null
        }
        if (inputInstructor.text.isNullOrBlank()) {
            tilInstructor.error = getString(R.string.error_required_field)
            valid = false
        } else {
            tilInstructor.error = null
        }
        tilSchedule.error = null
        tilRoom.error = null
        return valid
    }

    private fun setSavingUi(saving: Boolean) {
        progressSave.visibility = if (saving) android.view.View.VISIBLE else android.view.View.GONE
        buttonSaveCourse.isEnabled = !saving
        buttonCancel.isEnabled = !saving
    }
}
