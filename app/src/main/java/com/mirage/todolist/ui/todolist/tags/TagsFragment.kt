package com.mirage.todolist.ui.todolist.tags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.mirage.todolist.R
import com.mirage.todolist.databinding.FragmentTagsBinding
import com.mirage.todolist.di.App
import com.mirage.todolist.model.repository.LiveTag
import com.mirage.todolist.util.autoCleared
import javax.inject.Inject

/**
 * Main activity subscreen for "Tags" navigation option
 */
class TagsFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: TagsViewModel by viewModels { viewModelFactory }

    private var binding by autoCleared<FragmentTagsBinding>()

    var onToolbarUpListener: () -> Unit = {}
    var onTagSearchListener: (LiveTag) -> Unit = {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity().application as App).appComponent.inject(this)
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_tags,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeToolbar()
        initializeTags()
    }

    private fun initializeTags() {
        val chips = binding.tagFragmentChips
        chips.lifecycleOwner = viewLifecycleOwner
        chips.onTagClickListener = { tag ->
            val editTagDialog = TagEditDialogFragment()
            editTagDialog.tag = tag
            editTagDialog.onOptionSelectedListener = {
                when (it) {
                    TagEditDialogOption.RENAME -> {
                        openRenameDialog(tag)
                    }
                    TagEditDialogOption.RECOLOR -> {
                        openRecolorDialog(tag)
                    }
                    TagEditDialogOption.SEARCH -> {
                        goToSearchTasks(tag)
                    }
                    TagEditDialogOption.DELETE -> {
                        openDeleteTagDialog(tag)
                    }
                    TagEditDialogOption.CANCEL -> {}
                }
            }
            editTagDialog.show(childFragmentManager, "EditTagDialog")
        }
        chips.recreateTags(viewModel.getAllTags())
        viewModel.addOnFullUpdateTagListener(viewLifecycleOwner) { tags ->
            chips.recreateTags(tags)
        }
        viewModel.addOnNewTagListener(viewLifecycleOwner) { tag ->
            chips.addNewTag(tag)
        }
        viewModel.addOnRemoveTagListener(viewLifecycleOwner) { tag ->
            chips.removeTag(tag)
        }
    }

    private fun openNewTagDialog() {
        val createDialog = TagCreateDialogFragment()
        createDialog.onCreatePressed = { text ->
            if (text.isNotBlank()) {
                val tag = viewModel.createNewTag()
                viewModel.modifyTag(tag.tagId, newName = text.trim())
            }
        }
        createDialog.show(childFragmentManager, "CreateTagDialog")
    }

    private fun openRenameDialog(tag: LiveTag) {
        val renameDialog = TagRenameDialogFragment()
        renameDialog.onRenamePressed = { text ->
            if (text.isNotBlank()) {
                viewModel.modifyTag(tag.tagId, newName = text.trim())
            }
        }
        renameDialog.show(childFragmentManager, "RenameTagDialog")
    }

    private fun openRecolorDialog(tag: LiveTag) {
        val recolorDialog = TagRecolorDialogFragment()
        recolorDialog.onColorSelected = { colorIndex ->
            viewModel.modifyTag(tag.tagId, newStyleIndex = colorIndex)
        }
        recolorDialog.show(childFragmentManager, "RecolorTagDialog")
    }

    private fun goToSearchTasks(tag: LiveTag) {
        onTagSearchListener(tag)
    }

    private fun openDeleteTagDialog(tag: LiveTag) {
        val alertDialog = AlertDialog.Builder(requireActivity())
            .setTitle(R.string.tags_delete_title)
            .setMessage(R.string.tags_delete_subtitle)
            .setPositiveButton(R.string.tags_delete_yes) { _, _ ->
                viewModel.removeTag(tag)
            }
            .setNegativeButton(R.string.tags_delete_cancel) { _, _ -> }
            .create()
        alertDialog.show()
    }

    private fun initializeToolbar() {
        val toolbar = binding.tagsToolbar
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_drawer_open)
        toolbar.setNavigationOnClickListener {
            onToolbarUpListener()
        }
        toolbar.setOnMenuItemClickListener {
            openNewTagDialog()
            true
        }
    }
}