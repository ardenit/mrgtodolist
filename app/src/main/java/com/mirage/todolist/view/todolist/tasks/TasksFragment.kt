package com.mirage.todolist.view.todolist.tasks

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mirage.todolist.R
import com.mirage.todolist.databinding.TasksRootFragmentBinding
import com.mirage.todolist.model.tasks.getTodolistModel
import com.mirage.todolist.viewmodel.TasklistType

/**
 * Main activity subscreen for "Tasks" navigation option
 */
class TasksFragment : Fragment() {

    //TODO Inject
    private val todolistModel = getTodolistModel()

    private lateinit var sharedPreferences: SharedPreferences

    private var _binding: TasksRootFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TasksRootFragmentBinding.inflate(inflater, container, false)
        initializeViewPager()
        val btn: FloatingActionButton = binding.todolistNewTaskBtn
        btn.setOnClickListener {
            //TODO New task
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initializeViewPager() {
        val viewPager: ViewPager2 = binding.viewPager
        viewPager.offscreenPageLimit = TasklistType.typesCount
        viewPager.adapter = object : FragmentStateAdapter(childFragmentManager, lifecycle) {

            override fun getItemCount(): Int {
                return TasklistType.typesCount
            }

            override fun createFragment(position: Int): Fragment {
                return TaskRecyclerFragment.newInstance(position)
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