package com.mirage.todolist

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.iconRes
import com.mikepenz.materialdrawer.model.interfaces.nameRes
import com.mikepenz.materialdrawer.util.addItems
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView
import com.mirage.todolist.ui.main.PlaceholderFragment


class TodolistActivity : AppCompatActivity() {

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

        val viewPager: ViewPager2 = findViewById(R.id.view_pager)
        viewPager.offscreenPageLimit = 3
        viewPager.adapter = object : FragmentStateAdapter(supportFragmentManager, lifecycle) {
            override fun getItemCount(): Int {
                return 3
            }

            override fun createFragment(position: Int): Fragment {
                return PlaceholderFragment.newInstance(position + 1)
            }

        }
        val tabTitles = arrayOf(
            R.string.footer_archive_btn,
            R.string.footer_todo_btn,
            R.string.footer_done_btn
        )
        val tabIcons = arrayOf(
            R.drawable.ic_nav_archive,
            R.drawable.ic_nav_todo,
            R.drawable.ic_nav_done
        )
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.light_orange))
        tabs.setTabTextColors(ContextCompat.getColor(this, R.color.light_grey),
            ContextCompat.getColor(this, R.color.light_orange))
        TabLayoutMediator(tabs, viewPager, true, true) { tab, position ->
            tab.setText(tabTitles[position])
            tab.setIcon(tabIcons[position])
            tab.icon?.let {
                val colorStateList = ContextCompat.getColorStateList(this, R.color.todolist_footer_btn_color)
                val coloredIcon = DrawableCompat.wrap(it)
                DrawableCompat.setTintList(coloredIcon, colorStateList)
            }
        }.attach()
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
    }

}