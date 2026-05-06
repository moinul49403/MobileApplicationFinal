package com.university.studentcoursemanager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CourseDetailActivity : AppCompatActivity() {

    private lateinit var course: Course

    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
        val data = result.data
        if (data?.getBooleanExtra(IntentKeys.EXTRA_DELETED, false) == true) {
            finish()
            return@registerForActivityResult
        }
        val updated = data?.serializableExtra<Course>(IntentKeys.COURSE)
        if (updated != null) {
            course = updated
            bindCourseToViews()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_detail)

        val loaded = intent.serializableExtra<Course>(IntentKeys.COURSE)
        if (loaded == null) {
            Toast.makeText(this, R.string.error_save_failed, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        course = loaded

        findViewById<MaterialToolbar>(R.id.toolbarDetail).setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<FloatingActionButton>(R.id.fabEditCourse).setOnClickListener {
            val intent = Intent(this, EditCourseActivity::class.java)
                .putExtra(IntentKeys.COURSE, course)
            editLauncher.launch(intent)
        }

        bindCourseToViews()
    }

    private fun bindCourseToViews() {
        setRow(R.id.rowName, R.string.label_field_name, course.name)
        setRow(R.id.rowCode, R.string.label_field_code, course.code)
        setRow(R.id.rowInstructor, R.string.label_field_instructor, course.instructor)
        setRow(
            R.id.rowCreditHours,
            R.string.label_field_credit_hours,
            getString(R.string.credit_hours_format, course.creditHours),
        )
        setRow(R.id.rowSchedule, R.string.label_field_schedule, course.schedule)
        setRow(R.id.rowRoom, R.string.label_field_room, course.room)
        setRow(R.id.rowSemester, R.string.label_field_semester, course.semester)
    }

    private fun setRow(rowViewId: Int, labelRes: Int, value: CharSequence) {
        val row = findViewById<View>(rowViewId)
        row.findViewById<TextView>(R.id.textLabel).setText(labelRes)
        row.findViewById<TextView>(R.id.textValue).text = value
    }
}
