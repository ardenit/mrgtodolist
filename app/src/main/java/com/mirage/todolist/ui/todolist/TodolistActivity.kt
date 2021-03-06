package com.mirage.todolist.ui.todolist

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.navigation.NavigationView
import com.mirage.todolist.R
import com.mirage.todolist.di.App
import com.mirage.todolist.model.repository.LiveTask
import com.mirage.todolist.ui.edittask.EditTaskActivity
import com.mirage.todolist.ui.settings.SettingsActivity
import com.mirage.todolist.ui.todolist.tags.TagsFragment
import com.mirage.todolist.ui.todolist.tasks.TasksFragment
import javax.inject.Inject


class TodolistActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: TodolistViewModel by viewModels { viewModelFactory }

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private var navViewSelectedItem: Int = 0

    private lateinit var contentContainer: FrameLayout
    private lateinit var tasksFragment: TasksFragment
    private lateinit var tagsFragment: TagsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (activityInstancesCount != 0) {
            finish()
            return
        }
        ++activityInstancesCount
        (application as App).appComponent.inject(this)
        setContentView(R.layout.activity_todolist)
        initializeDrawer()
        initializeContentFragments()
    }

    override fun onBackPressed() {
        when {
            drawerLayout.isOpen -> {
                drawerLayout.close()
            }
            tasksFragment.isSearchOpened() -> {
                tasksFragment.closeSearch()
            }
            else -> {
                setResult(0)
                finish()
            }
        }
    }

    override fun onDestroy() {
        if (activityInstancesCount != 0) {
            --activityInstancesCount
        }
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(NAV_VIEW_OPENED_KEY, drawerLayout.isOpen)
        outState.putInt(NAV_VIEW_SELECTED_KEY, navigationView.checkedItem?.itemId ?: 0)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val navDrawerOpened = savedInstanceState.getBoolean(NAV_VIEW_OPENED_KEY, false)
        if (navDrawerOpened) {
            drawerLayout.open()
        }
        navViewSelectedItem = 0
        val navDrawerCheckedItemId = savedInstanceState.getInt(NAV_VIEW_SELECTED_KEY, 0)
        if (navDrawerCheckedItemId == R.id.nav_item_tags) {
            openTagsSubscreen()
            navViewSelectedItem = 1
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.tasks_toolbar_menu, menu)
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_item_tasks -> {
                openTasksSubscreen()
            }
            R.id.nav_item_tags -> {
                openTagsSubscreen()
            }
            R.id.nav_item_settings -> {
                openSettings()
            }
        }
        return true
    }

    private fun openTasksSubscreen() {
        supportFragmentManager.beginTransaction()
            .hide(tagsFragment)
            .show(tasksFragment)
            .commit()
        drawerLayout.close()
    }

    private fun openTagsSubscreen() {
        supportFragmentManager.beginTransaction()
            .hide(tasksFragment)
            .show(tagsFragment)
            .commit()
        drawerLayout.close()
    }

    private fun openSettings() {
        drawerLayout.close()
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun openTaskEditor(task: LiveTask?) {
        val intent = Intent(this, EditTaskActivity::class.java)
        if (task != null) {
            intent.putExtra(EditTaskActivity.EDITOR_TYPE_KEY, EditTaskActivity.EDITOR_TYPE_EDIT_TASK)
            intent.putExtra(EditTaskActivity.EDITOR_TASK_ID_KEY, task.taskId.toString())
        }
        else {
            intent.putExtra(EditTaskActivity.EDITOR_TYPE_KEY, EditTaskActivity.EDITOR_TYPE_CREATE_TASK)
        }
        intent.putExtra(EditTaskActivity.EDITOR_TASKLIST_ID_KEY, tasksFragment.getCurrentTasklistID())
        startActivity(intent)
    }

    private fun initializeDrawer() {
        drawerLayout = findViewById(R.id.act_main_root_layout)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        navigationView = findViewById(R.id.act_main_navigation_view)
        navigationView.setNavigationItemSelectedListener(this)
        navigationView.setCheckedItem(R.id.nav_item_tasks)
    }

    private fun initializeContentFragments() {
        contentContainer = findViewById(R.id.act_main_content)
        tasksFragment = TasksFragment()
        tasksFragment.onToolbarUpListener = {
            drawerLayout.open()
        }
        tasksFragment.onSearchQueryListener = {
            viewModel.searchTasks(it)
        }
        tasksFragment.onSearchStopListener = {
            viewModel.cancelTaskSearch()
        }
        tasksFragment.onEditTaskListener = {
            openTaskEditor(it)
        }
        tagsFragment = TagsFragment()
        tagsFragment.onToolbarUpListener = {
            drawerLayout.open()
        }
        tagsFragment.onTagSearchListener = { tag ->
            navigationView.setCheckedItem(R.id.nav_item_tasks)
            openTasksSubscreen()
            tasksFragment.openSearchForTag(tag)
        }
        if (supportFragmentManager.fragments.isNotEmpty()) {
            supportFragmentManager.beginTransaction().apply {
                supportFragmentManager.fragments.forEach {
                    remove(it)
                }
            }.commit()
        }
        supportFragmentManager.beginTransaction()
            .add(R.id.act_main_content, tasksFragment)
            .add(R.id.act_main_content, tagsFragment)
            .hide(tagsFragment)
            .commit()
        navigationView.setCheckedItem(navigationView.menu[0])
    }

    companion object {
        private const val NAV_VIEW_OPENED_KEY = "nav_view_opened"
        private const val NAV_VIEW_SELECTED_KEY = "nav_view_selected_item"

        /** Workaround for night theme bug with recreating activities */
        private var activityInstancesCount = 0
    }
}