package com.mirage.todolist.ui.todolist.tags

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.mirage.todolist.R
import com.mirage.todolist.model.repository.LiveTag

enum class TagEditDialogOption {
    RENAME,
    RECOLOR,
    SEARCH,
    DELETE,
    CANCEL
}

class TagEditDialogFragment : DialogFragment() {

    var onOptionSelectedListener: ((TagEditDialogOption) -> Unit)? = null
    var tag: LiveTag? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = activity?.let {
        val builder = AlertDialog.Builder(it)
            .setTitle(tag?.name?.value ?: "")
            .setItems(R.array.tag_edit_options) { _, index ->
                val options = TagEditDialogOption.values()
                onOptionSelectedListener?.invoke(options[index.coerceIn(options.indices)])
            }
        builder.create()
    } ?: error("Activity cannot be null")
}