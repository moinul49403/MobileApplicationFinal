package com.university.studentcoursemanager

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class EditCourseActivity : AppCompatActivity() {

    private lateinit var course: Course
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
    private lateinit var buttonUpdateCourse: MaterialButton
    private lateinit var buttonCancel: MaterialButton
    private lateinit var buttonDeleteCourse: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_course)

        val loaded = intent.serializableExtra<Course>(IntentKeys.COURSE)
        if (loaded == null || loaded.id.isEmpty()) {
            Toast.makeText(this, R.string.error_save_failed, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        course = loaded

        toolbar = findViewById(R.id.toolbarEdit)
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
        buttonUpdateCourse = findViewById(R.id.buttonUpdateCourse)
        buttonCancel = findViewById(R.id.buttonCancel)
        buttonDeleteCourse = findViewById(R.id.buttonDeleteCourse)

        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        pickerCreditHours.minValue = 1
        pickerCreditHours.maxValue = 4

        ArrayAdapter.createFromResource(
            this,
            R.array.semester_options,
            android.R.layout.simple_spinner_item,
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerSemester.adapter = adapter
        }

        inputCourseName.setText(course.name)
        inputCourseCode.setText(course.code)
        inputInstructor.setText(course.instructor)
        inputSchedule.setText(course.schedule)
        inputRoom.setText(course.room)
        pickerCreditHours.value = course.creditHours.coerceIn(1, 4)

        val semesters = resources.getStringArray(R.array.semester_options)
        val semesterIndex = semesters.indexOf(course.semester).takeIf { it >= 0 } ?: 0
        spinnerSemester.setSelection(semesterIndex)

        buttonCancel.setOnClickListener { finish() }

        buttonUpdateCourse.setOnClickListener { attemptUpdate() }

        buttonDeleteCourse.setOnClickListener { confirmDelete() }
    }

    private fun attemptUpdate() {
        if (!validateRequiredFields()) return

        val updated = Course(
            id = course.id,
            name = inputCourseName.text?.toString()?.trim().orEmpty(),
            code = inputCourseCode.text?.toString()?.trim().orEmpty(),
            instructor = inputInstructor.text?.toString()?.trim().orEmpty(),
            creditHours = pickerCreditHours.value,
            schedule = inputSchedule.text?.toString()?.trim().orEmpty(),
            room = inputRoom.text?.toString()?.trim().orEmpty(),
            semester = spinnerSemester.selectedItem?.toString().orEmpty(),
        )

        setSavingUi(true)
        FirebaseCourses.ref(this).child(course.id).setValue(updated)
            .addOnCompleteListener { task ->
                setSavingUi(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, R.string.toast_course_updated, Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK, Intent().putExtra(IntentKeys.COURSE, updated))
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

    private fun confirmDelete() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_delete_course_title)
            .setMessage(R.string.dialog_delete_course_message)
            .setPositiveButton(R.string.dialog_delete_confirm) { _, _ -> performDelete() }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }

    private fun performDelete() {
        setSavingUi(true)
        FirebaseCourses.ref(this).child(course.id).removeValue()
            .addOnCompleteListener { task ->
                setSavingUi(false)
                if (task.isSuccessful) {
                    setResult(
                        RESULT_OK,
                        Intent().putExtra(IntentKeys.EXTRA_DELETED, true),
                    )
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
        buttonUpdateCourse.isEnabled = !saving
        buttonCancel.isEnabled = !saving
        buttonDeleteCourse.isEnabled = !saving
    }
}
