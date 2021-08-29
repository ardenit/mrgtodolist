package com.mirage.todolist.view.edittask

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.mirage.todolist.R
import kotlinx.coroutines.selects.select

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