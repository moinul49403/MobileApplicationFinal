package com.university.studentcoursemanager

import java.io.Serializable

data class Course(
    var id: String = "",
    var name: String = "",
    var code: String = "",
    var instructor: String = "",
    var creditHours: Int = 0,
    var schedule: String = "",
    var room: String = "",
    var semester: String = "",
) : Serializable
