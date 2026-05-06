package com.university.studentcoursemanager

import android.content.Context
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseCourses {
    private const val PATH_COURSES = "courses"

    fun ref(context: Context): DatabaseReference {
        val studentId = StudentIdStore.getStudentId(context)
        return FirebaseDatabase.getInstance().reference.child(PATH_COURSES).child(studentId)
    }
}
