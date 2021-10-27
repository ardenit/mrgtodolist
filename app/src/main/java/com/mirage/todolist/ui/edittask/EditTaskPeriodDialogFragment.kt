package com.mirage.todolist.ui.edittask

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.mirage.todolist.R

class EditTaskPeriodDialogFragment : DialogFragment() {

    var onPeriodSelected: ((Int) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
            .setTitle(R.string.edit_task_period_title)
            .setItems(R.array.task_periods) { _, index ->
                onPeriodSelected?.invoke(index)
            }
        return builder.create()
    }
}