package com.mirage.todolist.view.todolist.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mirage.todolist.R
import com.mirage.todolist.databinding.TasksRootFragmentBinding
import com.mirage.todolist.model.repository.LiveTag
import com.mirage.todolist.model.repository.LiveTask
import com.mirage.todolist.viewmodel.TasklistType

/**
 * Main activity subscreen for "Tasks" navigation option
 */
class TasksFragment : Fragment() {

    var onToolbarUpListener: () -> Unit = {}
    var onSearchQueryListener: (String) -> Unit = {}
    var onSearchStopListener: () -> Unit = {}
    var onEditTaskListener: (LiveTask?) -> Unit = {}
    private var _binding: TasksRootFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TasksRootFragmentBinding.inflate(inflater, container, false)
        initializeViewPager()
        initializeToolbar()
        val btn: FloatingActionButton = binding.todolistNewTaskBtn
        btn.setOnClickListener {
            onEditTaskListener(null)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun openSearchForTag(tag: LiveTag) {
        val toolbar = binding.tasksToolbar
        val searchItem = toolbar.menu[0]
        val searchView = searchItem.actionView as SearchView
        toolbar.menu.performIdentifierAction(R.id.toolbar_search_btn, 0)
        searchView.setQuery("[${tag.name.value}] ", false)
    }

    fun isSearchOpened(): Boolean {
        val searchView = binding.tasksToolbar.menu[0].actionView as SearchView
        return !searchView.isIconified
    }

    fun closeSearch() {
        val menuItem = binding.tasksToolbar.menu[0]
        menuItem.collapseActionView()
        onSearchStopListener()
    }

    fun getCurrentTasklistID(): Int {
        return binding.viewPager.currentItem
    }

    private fun initializeToolbar() {
        val toolbar = binding.tasksToolbar
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_drawer_open)
        toolbar.setNavigationOnClickListener {
            onToolbarUpListener()
        }
        val searchItem = toolbar.menu[0]
        val searchView = searchItem.actionView as SearchView
        searchView.setOnCloseListener {
            onSearchStopListener()
            false
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    onSearchQueryListener(query)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    onSearchQueryListener(newText)
                }
                return false
            }
        })
    }

    private fun initializeViewPager() {
        val viewPager: ViewPager2 = binding.viewPager
        viewPager.offscreenPageLimit = TasklistType.typesCount
        viewPager.adapter = object : FragmentStateAdapter(childFragmentManager, lifecycle) {

            override fun getItemCount(): Int {
                return TasklistType.typesCount
            }

            override fun createFragment(position: Int): Fragment {
                val fragment = TaskRecyclerFragment.newInstance(position)
                fragment.onSearchTagListener = { tag ->
                    openSearchForTag(tag)
                }
                fragment.onTaskEditListener = { task ->
                    onEditTaskListener(task)
                }
                return fragment
            }

        }
        if (viewPager.currentItem != 1) {
            viewPager.setCurrentItem(1, false)
        }
        viewPager.isUserInputEnabled = false
        val tabs: TabLayout = binding.tabs
        tabs.setSelectedTabIndicatorColor(ContextCompat.getColor(requireContext(), R.color.light_orange))
        tabs.setTabTextColors(
            ContextCompat.getColor(requireContext(), R.color.light_grey),
            ContextCompat.getColor(requireContext(), R.color.light_orange))
        TabLayoutMediator(tabs, viewPager, true, true) { tab, position ->
            val type = TasklistType.getType(position)
            tab.setText(type.title)
            tab.setIcon(type.icon)
            tab.icon?.let {
                val colorStateList = ContextCompat.getColorStateList(requireContext(),
                    R.color.todolist_footer_btn_color
                )
                val coloredIcon = DrawableCompat.wrap(it)
                DrawableCompat.setTintList(coloredIcon, colorStateList)
            }
        }.attach()
    }
}