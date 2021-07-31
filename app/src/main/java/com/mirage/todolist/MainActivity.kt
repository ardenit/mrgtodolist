package com.mirage.todolist

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.iconRes
import com.mikepenz.materialdrawer.model.interfaces.nameRes
import com.mikepenz.materialdrawer.util.addItems
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawerSlider: MaterialDrawerSliderView

    private lateinit var tasksDrawerItem: PrimaryDrawerItem
    private lateinit var tagsDrawerItem: PrimaryDrawerItem
    private lateinit var settingsDrawerItem: SecondaryDrawerItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.todolist_root)
        initializeDrawer()
        initializeToolbar()
        toolbar.setNavigationOnClickListener {
            drawerLayout.open()
        }
        drawerSlider = findViewById(R.id.act_main_nav_slider)
    }

    private fun initializeDrawer() {
        drawerLayout = findViewById(R.id.act_main_root_layout)
        drawerSlider = findViewById(R.id.act_main_nav_slider)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        drawerSlider.headerView = object : FrameLayout(this) {
            init {
                inflate(context, R.layout.nav_header, this)
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
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        return true
    }

}