package com.mirage.todolist.view.todolist.tags

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.mirage.todolist.R

class TagRenameDialogFragment : DialogFragment() {

    private lateinit var inputText: EditText

    var onRenamePressed: ((String) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(requireContext(), R.layout.tags_rename_dialog, null)
        inputText = view.findViewById(R.id.tags_rename_input)
        val builder = AlertDialog.Builder(requireActivity())
            .setTitle(resources.getString(R.string.tags_rename_title))
            .setView(view)
            .setPositiveButton(R.string.tags_rename_tag_option) { _, _ ->
                onRenamePressed?.invoke(inputText.text.toString())
            }
            .setNegativeButton(R.string.tags_cancel_option) { _, _ -> }
        return builder.create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val savedText = savedInstanceState?.getString(INPUT_TEXT_KEY)
        if (savedText != null) {
            inputText.setText(savedText)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(INPUT_TEXT_KEY, inputText.text.toString())
        super.onSaveInstanceState(outState)
    }

    companion object {

        private const val INPUT_TEXT_KEY = "input_text"
    }
}