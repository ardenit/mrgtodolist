package com.mirage.todolist.view.edittask

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.mirage.todolist.R
import com.mirage.todolist.model.tasks.LiveTag
import com.mirage.todolist.model.tasks.TagID
import com.mirage.todolist.view.todolist.tags.TagsView

class EditTaskAddTagDialogFragment : DialogFragment() {

    private lateinit var tagsView: TagsView

    var tags: List<LiveTag>? = null
    var onTagSelected: ((LiveTag) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(requireContext(), R.layout.edit_task_add_tag_dialog, null)
        tagsView = view.findViewById(R.id.edit_task_add_tag_tags)
        tagsView.lifecycleOwner = this
        tagsView.onTagClickListener = {
            onTagSelected?.invoke(it)
        }
        tags?.let { tagsView.recreateTags(it) }
        val builder = AlertDialog.Builder(requireActivity())
            .setTitle(resources.getString(R.string.edit_task_tags_title))
            .setView(view)
            .setNegativeButton(R.string.tags_cancel_option) { _, _ -> }
        return builder.create()
    }
}