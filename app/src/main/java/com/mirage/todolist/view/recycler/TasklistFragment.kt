package com.mirage.todolist.view.recycler

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mirage.todolist.databinding.TodolistContentFragmentBinding
import com.mirage.todolist.viewmodel.*

class TasklistFragment : Fragment() {

    private lateinit var tasklistViewModel: TasklistViewModel
    private lateinit var recycler: RecyclerView
    private lateinit var recyclerAdapter: TasklistRecyclerAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper

    private var _binding: TodolistContentFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val parentViewModel = ViewModelProvider(this.requireActivity()).get(TodolistViewModelImpl::class.java)
        tasklistViewModel = ViewModelProvider(this).get(TasklistViewModelImpl::class.java).apply {
            val tasklistID = arguments?.getInt(ARG_SECTION_NUMBER) ?: 1
            init(parentViewModel, tasklistID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TodolistContentFragmentBinding.inflate(inflater, container, false)
        val root = binding.root
        initializeRecycler()
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initializeRecycler() {
        recycler = binding.todolistRecyclerView
        recycler.layoutManager = LinearLayoutManager(context)
        recyclerAdapter = TasklistRecyclerAdapter(requireContext(), tasklistViewModel, viewLifecycleOwner)
        recycler.adapter = recyclerAdapter
        val itemTouchHelperCallback = TasklistItemTouchHelperCallback(recyclerAdapter, tasklistViewModel.getTasklistID())
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
        fun newInstance(sectionNumber: Int): TasklistFragment {
            return TasklistFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}