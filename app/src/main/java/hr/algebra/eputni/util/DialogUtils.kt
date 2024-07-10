package hr.algebra.eputni.util

import android.content.Context
import androidx.appcompat.app.AlertDialog
import hr.algebra.eputni.R

object DialogUtils {

    fun showDeleteConfirmationDialog(
        context: Context,
        title: String,
        message: String,
        onConfirm: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(context.getString(R.string.ok)) { dialog, _ ->
                onConfirm()
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}