package com.university.studentcoursemanager

import android.content.Context
import java.util.UUID

object StudentIdStore {
    private const val PREFS = "student_course_manager_prefs"
    private const val KEY_STUDENT_ID = "student_id"

    fun getStudentId(context: Context): String {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY_STUDENT_ID, null)
        if (existing != null) return existing
        val generated = UUID.randomUUID().toString()
        prefs.edit().putString(KEY_STUDENT_ID, generated).apply()
        return generated
    }
}
