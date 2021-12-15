package com.daniluk.screens

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.daniluk.MainViewModel

class ConfirmationDialogFragment(
) : DialogFragment() {
    val viewmodel = MainViewModel.instansViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var builder =
            AlertDialog.Builder(requireContext())
                .setTitle(viewmodel.title_v)
                .setMessage(viewmodel.message_v)
                .setCancelable(viewmodel.cancelable_v)

        if (! viewmodel.positiveButton_v.isEmpty()) {
            builder.setPositiveButton(viewmodel.positiveButton_v, { _, _ -> viewmodel.positiveFun_v() })
        }
        if (! viewmodel.negativeButton_v.isEmpty()) {
            builder.setNegativeButton(viewmodel.negativeButton_v, { _, _ -> viewmodel.negativeFun_v() })
        }
        if (! viewmodel.neutralButton_v.isEmpty()) {
            builder.setNeutralButton(viewmodel.neutralButton_v, { _, _ -> viewmodel.neutralFun_v() })
        }
        return builder.create()
    }

    companion object {
        const val TAG = "PurchaseConfirmationDialog_2"
    }

}