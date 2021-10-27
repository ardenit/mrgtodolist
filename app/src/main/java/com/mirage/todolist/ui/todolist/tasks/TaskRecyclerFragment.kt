package com.mirage.todolist.ui.todolist.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mirage.todolist.R
import com.mirage.todolist.databinding.FragmentTasklistBinding
import com.mirage.todolist.di.App
import com.mirage.todolist.model.repository.LiveTag
import com.mirage.todolist.model.repository.LiveTask
import com.mirage.todolist.util.autoCleared
import javax.inject.Inject

/**
 * Container for a single tasklist (e.g. archived, completed, current tasks) inside [TasksFragment]
 */
class TaskRecyclerFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val taskRecyclerViewModel: TaskRecyclerViewModel by viewModels { viewModelFactory }
    private lateinit var recycler: RecyclerView
    private lateinit var recyclerAdapter: TasklistRecyclerAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper

    private var binding by autoCleared<FragmentTasklistBinding>()

    var onSearchTagListener: (LiveTag) -> Unit = {}
    var onTaskEditListener: (LiveTask) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tasklistID = arguments?.getInt(ARG_SECTION_NUMBER) ?: 1
        taskRecyclerViewModel.init(tasklistID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity().application as App).appComponent.inject(this)
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_tasks,
            container,
            false
        )
        initializeRecycler()
        return binding.root
    }

    private fun initializeRecycler() {
        recycler = binding.todolistRecyclerView
        recycler.layoutManager = LinearLayoutManager(context)
        recyclerAdapter = TasklistRecyclerAdapter(requireContext(), taskRecyclerViewModel, viewLifecycleOwner, onSearchTagListener, onTaskEditListener)
        recycler.adapter = recyclerAdapter
        val itemTouchHelperCallback = TasklistItemTouchHelperCallback(recyclerAdapter, taskRecyclerViewModel.getTasklistID())
        itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recycler)
        val divider = TasklistItemDecoration()
        recycler.addItemDecoration(divider)
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): TaskRecyclerFragment {
            return TaskRecyclerFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}