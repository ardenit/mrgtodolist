package com.mirage.todolist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.common.AccountPicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.iconRes
import com.mikepenz.materialdrawer.model.interfaces.nameRes
import com.mikepenz.materialdrawer.util.addItems
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView
import com.mirage.todolist.content.TasklistFragment
import com.mirage.todolist.content.TasklistType
import com.mirage.todolist.model.gdrive.GDriveConnectExceptionHandler
import com.mirage.todolist.model.todolistModel


class TodolistActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawerSlider: MaterialDrawerSliderView

    private lateinit var tasksDrawerItem: PrimaryDrawerItem
    private lateinit var tagsDrawerItem: PrimaryDrawerItem
    private lateinit var settingsDrawerItem: SecondaryDrawerItem

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
        initializeDrawer()
        initializeToolbar()
        initializeViewPager()

        val btn: FloatingActionButton = findViewById(R.id.todolist_new_task_btn)
        btn.setOnClickListener(::onGDriveSyncBtnPressed)
        todolistModel.init(this)
        if (savedInstanceState == null) {

        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isOpen) {
            drawerLayout.close()
        }
        else {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("nav_drawer_opened", drawerLayout.isOpen)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val navDrawerOpened = savedInstanceState.getBoolean("nav_drawer_opened", false)
        if (navDrawerOpened) {
            drawerLayout.open()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        return true
    }

    private fun onResultFromAccPicker(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val extras = result.data?.extras
            val authAccount = extras?.getString("authAccount")
            todolistModel.setGDriveAccount(authAccount)
            todolistModel.connectToGDriveAccount(gDriveConnectExceptionHandler)
        }
        else {
            Toast.makeText(this, R.string.gdrive_sync_cancelled_toast, Toast.LENGTH_SHORT).show()
        }
    }

    private fun onResultFromGDriveUserIntervene(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            todolistModel.setGDriveAccount(todolistModel.getGDriveAccountEmail())
            todolistModel.connectToGDriveAccount(gDriveConnectExceptionHandler)
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

    private fun initializeDrawer() {
        drawerLayout = findViewById(R.id.act_main_root_layout)
        drawerSlider = findViewById(R.id.act_main_nav_slider)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        drawerSlider.headerView = object : FrameLayout(this) {
            init {
                inflate(context, R.layout.todolist_drawer_header, this)
            }
        }
        tasksDrawerItem = PrimaryDrawerItem().apply {
            nameRes = R.string.drawer_btn_tasks
            iconRes = R.drawable.ic_drawer_tasks
        }
        tagsDrawerItem = PrimaryDrawerItem().apply {
            nameRes = R.string.drawer_btn_tags
            iconRes = R.drawable.ic_drawer_tags
        }
        settingsDrawerItem = SecondaryDrawerItem().apply {
            nameRes = R.string.drawer_btn_settings
            iconRes = R.drawable.ic_drawer_settings
        }
        drawerSlider.addItems(tasksDrawerItem, tagsDrawerItem, DividerDrawerItem(), settingsDrawerItem)
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

    private fun initializeViewPager() {
        val viewPager: ViewPager2 = findViewById(R.id.view_pager)
        viewPager.offscreenPageLimit = TasklistType.typesCount
        viewPager.adapter = object : FragmentStateAdapter(supportFragmentManager, lifecycle) {
            override fun getItemCount(): Int {
                return TasklistType.typesCount
            }

            override fun createFragment(position: Int): Fragment {
                return TasklistFragment.newInstance(position + 1)
            }

        }
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.light_orange))
        tabs.setTabTextColors(ContextCompat.getColor(this, R.color.light_grey),
            ContextCompat.getColor(this, R.color.light_orange))
        TabLayoutMediator(tabs, viewPager, true, true) { tab, position ->
            val type = TasklistType.getType(position)
            tab.setText(type.title)
            tab.setIcon(type.icon)
            tab.icon?.let {
                val colorStateList = ContextCompat.getColorStateList(this, R.color.todolist_footer_btn_color)
                val coloredIcon = DrawableCompat.wrap(it)
                DrawableCompat.setTintList(coloredIcon, colorStateList)
            }
        }.attach()
        viewPager.currentItem = 1
    }

}