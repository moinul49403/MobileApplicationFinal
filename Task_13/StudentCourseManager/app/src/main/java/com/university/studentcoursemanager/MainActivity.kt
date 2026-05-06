package com.university.studentcoursemanager

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var recyclerCourses: RecyclerView
    private lateinit var fabAddCourse: FloatingActionButton
    private lateinit var emptyStateContainer: android.view.View

    private lateinit var adapter: CourseAdapter
    private var coursesListener: ValueEventListener? = null
    private var searchQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        recyclerCourses = findViewById(R.id.recyclerCourses)
        fabAddCourse = findViewById(R.id.fabAddCourse)
        emptyStateContainer = findViewById(R.id.emptyStateContainer)

        setSupportActionBar(toolbar)

        adapter = CourseAdapter(
            onCardClick = { course -> openCourseDetail(course) },
            onEditClick = { course -> openEditCourse(course) },
            onDeleteClick = { course -> confirmDelete(course) },
            onDisplayedCountChanged = { count -> updateEmptyState(count == 0) },
        )
        recyclerCourses.layoutManager = LinearLayoutManager(this)
        recyclerCourses.adapter = adapter

        fabAddCourse.setOnClickListener {
            startActivity(Intent(this, AddCourseActivity::class.java))
        }

        attachFirebaseListener()
        updateEmptyState(adapter.itemCount == 0)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = getString(R.string.search_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                searchQuery = newText.orEmpty()
                adapter.setFilterQuery(searchQuery)
                return true
            }
        })
        if (searchQuery.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(searchQuery, false)
        }
        return true
    }

    private fun attachFirebaseListener() {
        val ref = FirebaseCourses.ref(this)
        coursesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Course>()
                for (child in snapshot.children) {
                    val c = child.getValue(Course::class.java) ?: continue
                    c.id = child.key ?: c.id
                    list.add(c)
                }
                list.sortBy { it.name.lowercase() }
                adapter.submitFirebaseCourses(list)
            }

            override fun onCancelled(error: DatabaseError) {
                adapter.submitFirebaseCourses(emptyList())
            }
        }
        coursesListener?.let { ref.addValueEventListener(it) }
    }

    private fun updateEmptyState(listEmpty: Boolean) {
        emptyStateContainer.visibility =
            if (listEmpty) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun confirmDelete(course: Course) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_delete_course_main_title)
            .setMessage(R.string.dialog_delete_course_main_message)
            .setPositiveButton(R.string.dialog_delete_confirm) { _, _ ->
                FirebaseCourses.ref(this).child(course.id).removeValue()
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }

    private fun openCourseDetail(course: Course) {
        startActivity(
            Intent(this, CourseDetailActivity::class.java)
                .putExtra(IntentKeys.COURSE, course),
        )
    }

    private fun openEditCourse(course: Course) {
        startActivity(
            Intent(this, EditCourseActivity::class.java)
                .putExtra(IntentKeys.COURSE, course),
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        coursesListener?.let { FirebaseCourses.ref(this).removeEventListener(it) }
        coursesListener = null
    }
}
