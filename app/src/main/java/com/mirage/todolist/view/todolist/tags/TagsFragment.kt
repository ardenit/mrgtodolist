package com.mirage.todolist.view.todolist.tags

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.google.android.material.chip.Chip
import com.mirage.todolist.R
import com.mirage.todolist.databinding.TagsRootFragmentBinding
import com.mirage.todolist.model.tasks.LiveTag
import com.mirage.todolist.model.tasks.TagID
import com.mirage.todolist.model.tasks.TodolistModel
import com.mirage.todolist.model.tasks.getTodolistModel
import com.mirage.todolist.viewmodel.TagsViewModel
import com.mirage.todolist.viewmodel.TagsViewModelImpl

/**
 * Main activity subscreen for "Tags" navigation option
 */
class TagsFragment : Fragment() {

    //TODO Inject
    private val todolistModel: TodolistModel = getTodolistModel()
    private lateinit var tagsViewModel: TagsViewModel

    var onToolbarUpListener: () -> Unit = {}
    private var _binding: TagsRootFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TagsRootFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tagsViewModel = ViewModelProvider(this).get(TagsViewModelImpl::class.java)
        tagsViewModel.init()
        initializeToolbar()
        recreateChips(tagsViewModel.getAllTags())
        tagsViewModel.addOnFullUpdateTagListener(this, ::recreateChips)
    }

    private fun recreateChips(tags: Map<TagID, LiveTag>) {
        val chips = binding.tagFragmentChips
        chips.removeAllViews()
        val tagList = tags.values.sortedBy { it.tagIndex }
        tagList.forEach { tag ->
            val chip = Chip(requireContext())
            tag.name.observe(viewLifecycleOwner) {
                chip.text = it
            }
            tag.color.observe(viewLifecycleOwner) {
                chip.setBackgroundColor(it)
            }
            tag.textColor.observe(viewLifecycleOwner) {
                chip.setTextColor(it)
            }
            chip.setOnClickListener {
                println("click ${tag.name}")
                //TODO
            }
            chips.addView(chip)
        }
    }

    private fun initializeToolbar() {
        val toolbar = binding.tagsToolbar
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_drawer_open)
        toolbar.setNavigationOnClickListener {
            onToolbarUpListener()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}