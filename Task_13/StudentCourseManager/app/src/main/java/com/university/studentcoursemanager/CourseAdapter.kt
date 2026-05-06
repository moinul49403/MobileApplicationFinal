package com.university.studentcoursemanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class CourseAdapter(
    private val onCardClick: (Course) -> Unit,
    private val onEditClick: (Course) -> Unit,
    private val onDeleteClick: (Course) -> Unit,
    private val onDisplayedCountChanged: (Int) -> Unit = {},
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    private val allCourses = mutableListOf<Course>()
    private val displayedCourses = mutableListOf<Course>()
    private var filterQuery: String = ""

    fun submitFirebaseCourses(courses: List<Course>) {
        allCourses.clear()
        allCourses.addAll(courses)
        applyFilter()
    }

    fun setFilterQuery(query: String) {
        filterQuery = query.trim()
        applyFilter()
    }

    private fun applyFilter() {
        displayedCourses.clear()
        if (filterQuery.isEmpty()) {
            displayedCourses.addAll(allCourses)
        } else {
            val q = filterQuery.lowercase()
            allCourses.filterTo(displayedCourses) { course ->
                course.name.lowercase().contains(q) || course.code.lowercase().contains(q)
            }
        }
        notifyDataSetChanged()
        onDisplayedCountChanged(displayedCourses.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(displayedCourses[position])
    }

    override fun getItemCount(): Int = displayedCourses.size

    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.cardCourse)
        private val textCourseName: TextView = itemView.findViewById(R.id.textCourseName)
        private val textCourseCode: TextView = itemView.findViewById(R.id.textCourseCode)
        private val textInstructor: TextView = itemView.findViewById(R.id.textInstructor)
        private val textCreditHours: TextView = itemView.findViewById(R.id.textCreditHours)
        private val textSchedule: TextView = itemView.findViewById(R.id.textSchedule)
        private val buttonEdit: ImageButton = itemView.findViewById(R.id.buttonEdit)
        private val buttonDelete: ImageButton = itemView.findViewById(R.id.buttonDelete)

        fun bind(course: Course) {
            textCourseName.text = course.name
            textCourseCode.text = course.code
            textInstructor.text = course.instructor
            textCreditHours.text = itemView.context.getString(R.string.credit_hours_format, course.creditHours)
            textSchedule.text = course.schedule

            card.setOnClickListener { onCardClick(course) }
            buttonEdit.setOnClickListener { onEditClick(course) }
            buttonDelete.setOnClickListener { onDeleteClick(course) }
        }
    }
}
