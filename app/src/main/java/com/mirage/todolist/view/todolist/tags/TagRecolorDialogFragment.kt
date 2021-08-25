package com.mirage.todolist.view.todolist.tags

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.mirage.todolist.R
import kotlinx.coroutines.selects.select

class TagRecolorDialogFragment : DialogFragment() {

    private lateinit var scrollContainer: LinearLayout

    var onColorSelected: ((Int) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(requireContext(), R.layout.tags_recolor_dialog, null)
        scrollContainer = view.findViewById(R.id.tags_recolor_container)
        initializeColorSelectButtons()
        val builder = AlertDialog.Builder(requireActivity())
            .setTitle(resources.getString(R.string.tags_recolor_title))
            .setView(view)
            .setNegativeButton(R.string.tags_cancel_option) { _, _ -> }
        return builder.create()
    }

    private fun initializeColorSelectButtons() {
        TagStyle.values().forEachIndexed { index, tagStyle ->
            val selectBtn = Button(requireContext())
            selectBtn.setText(tagStyle.colorName)
            val colorStateList = ContextCompat.getColorStateList(requireContext(), tagStyle.backgroundColor)
            selectBtn.setBackgroundColor(colorStateList?.defaultColor ?: Color.WHITE)
            selectBtn.setTextColor(ContextCompat.getColor(requireContext(), tagStyle.textColor))
            selectBtn.textSize = resources.getDimension(R.dimen.tag_recolor_btn_text_size)
            selectBtn.isAllCaps = false
            selectBtn.setOnClickListener {
                onColorSelected?.invoke(index)
                dismiss()
            }
            scrollContainer.addView(selectBtn)
        }
    }
}