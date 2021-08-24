package com.mirage.todolist.view.todolist.tags

import android.content.SharedPreferences
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
        initializeTags()
    }

    private fun initializeTags() {
        val chips = binding.tagFragmentChips
        chips.lifecycleOwner = viewLifecycleOwner
        chips.onTagClickListener = { tag ->
            println("click ${tag.name}")
        }
        chips.recreateTags(tagsViewModel.getAllTags())
        tagsViewModel.addOnFullUpdateTagListener(viewLifecycleOwner) { tags ->
            chips.recreateTags(tags)
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