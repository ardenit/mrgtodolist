package com.mirage.todolist.view.todolist

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.common.AccountPicker
import com.google.android.material.navigation.NavigationView
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.mirage.todolist.R
import com.mirage.todolist.model.gdrive.GDriveConnectExceptionHandler
import com.mirage.todolist.model.tasks.TodolistModel
import com.mirage.todolist.model.tasks.getTodolistModel
import com.mirage.todolist.view.settings.SettingsActivity
import com.mirage.todolist.view.todolist.tags.TagsFragment
import com.mirage.todolist.view.todolist.tasks.TasksFragment


class TodolistActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var toolbar: Toolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var contentContainer: FrameLayout
    private lateinit var tasksFragment: TasksFragment
    private lateinit var tagsFragment: TagsFragment

    //TODO Inject
    private val todolistModel: TodolistModel = getTodolistModel()

    /**
     * Activity result launcher for Google Drive synchronization account picker screen
     */
    private val accPickerResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        onResultFromAccPicker(result)
    }

    /**
     * Activity result launcher for Google Drive [UserRecoverableAuthIOException] user intervene screen
     */
    private val gDriveUserInterveneResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        onResultFromGDriveUserIntervene(result)
    }

    private val gDriveConnectExceptionHandler = object : GDriveConnectExceptionHandler {

        override suspend fun onSuccessfulConnect() {
            println("GDRIVE_CONNECT_SUCCESSFUL")
            Toast.makeText(this@TodolistActivity, "OK", Toast.LENGTH_SHORT).show()
        }

        override suspend fun onUserRecoverableFailure(ex: UserRecoverableAuthIOException) {
            println("USER_RECOVERABLE")
            gDriveUserInterveneResultLauncher.launch(ex.intent)
        }

        override suspend fun onGoogleAuthFailure(ex: GoogleAuthIOException) {
            println("GOOGLE_AUTH_FAIL")
            println(ex.message)
            Toast.makeText(this@TodolistActivity, "GOOGLE_AUTH_FAILURE SEE LOGS", Toast.LENGTH_SHORT).show()
        }

        override suspend fun onUnspecifiedFailure(ex: Exception) {
            println("UNSPECIFIED_GDRIVE_CONNECT_FAILURE")
            println(ex.message)
            Toast.makeText(this@TodolistActivity, "UNSPECIFIED_GDRIVE_CONNECT_FAILURE SEE LOGS", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.todolist_root)
        initializePreferences()
        initializeDrawer()
        initializeToolbar()
        contentContainer = findViewById(R.id.act_main_content)
        tasksFragment = TasksFragment()
        tagsFragment = TagsFragment()
        supportFragmentManager.beginTransaction()
            .add(R.id.act_main_content, tasksFragment)
            .add(R.id.act_main_content, tagsFragment)
            .hide(tagsFragment)
            .commit()
        if (activityInstancesCount != 0) {
            finish()
        }
        else {
            ++activityInstancesCount
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isOpen) {
            drawerLayout.close()
        }
        else {
            setResult(0)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activityInstancesCount != 0) {
            --activityInstancesCount
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("nav_drawer_opened", drawerLayout.isOpen)
        outState.putInt("nav_drawer_option", navigationView.checkedItem?.itemId ?: 0)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val navDrawerOpened = savedInstanceState.getBoolean("nav_drawer_opened", false)
        if (navDrawerOpened) {
            drawerLayout.open()
        }
        val navDrawerCheckedItemId = savedInstanceState.getInt("nav_drawer_option", 0)
        navigationView.setCheckedItem(navDrawerCheckedItemId)
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
        toolbar.title = resources.getString(R.string.drawer_btn_tasks)
        toolbar.menu.getItem(0).isVisible = true
        drawerLayout.close()
    }

    private fun openTagsSubscreen() {
        supportFragmentManager.beginTransaction()
            .hide(tasksFragment)
            .show(tagsFragment)
            .commit()
        toolbar.title = resources.getString(R.string.drawer_btn_tags)
        toolbar.menu.getItem(0).isVisible = false
        drawerLayout.close()
    }

    private fun onResultFromAccPicker(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val extras = result.data?.extras
            val authAccount = extras?.getString("authAccount")
            todolistModel.setGDriveAccountEmail(authAccount, gDriveConnectExceptionHandler)
        }
        else {
            Toast.makeText(this, R.string.gdrive_sync_cancelled_toast, Toast.LENGTH_SHORT).show()
        }
    }

    private fun onResultFromGDriveUserIntervene(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            todolistModel.setGDriveAccountEmail(todolistModel.getGDriveAccountEmail(), gDriveConnectExceptionHandler)
        }
        else {
            Toast.makeText(this, R.string.gdrive_sync_cancelled_toast, Toast.LENGTH_SHORT).show()
        }
    }

    private fun onGDriveSyncBtnPressed(syncBtn: View) {
        val options = AccountPicker.AccountChooserOptions.Builder()
            .setAllowableAccountsTypes(listOf(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE))
            .setAlwaysShowAccountPicker(true)
            .setTitleOverrideText(resources.getString(R.string.gdrive_acc_picker_title))
            .build()
        val intent = AccountPicker.newChooseAccountIntent(options)
        accPickerResultLauncher.launch(intent)
    }

    private fun openSettings() {
        drawerLayout.close()
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun initializePreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
    }

    private fun initializeDrawer() {
        drawerLayout = findViewById(R.id.act_main_root_layout)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        navigationView = findViewById(R.id.act_main_navigation_view)
        navigationView.setNavigationItemSelectedListener(this)
        navigationView.setCheckedItem(R.id.nav_item_tasks)
    }

    private fun initializeToolbar() {
        toolbar = findViewById(R.id.toolbar_actionbar)
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_drawer_open)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            drawerLayout.open()
        }
    }

    companion object {

        /** Workaround for night theme bug with recreating activities */
        private var activityInstancesCount = 0
    }
}