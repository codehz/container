package one.codehz.container.widget

import android.content.Context
import android.support.design.widget.BottomSheetDialog
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import one.codehz.container.R
import one.codehz.container.ext.get

class MyBottomSheetDialog(context: Context, title: String, description: String, callback: () -> Unit)
    : BaseBottomSheetDialog(context, R.layout.bottom_dialog) {
    val titleView by lazy<TextView> { this[R.id.title] }
    val descriptionView by lazy<TextView> { this[R.id.description] }
    val dismissButton by lazy<Button> { this[R.id.dismiss] }

    init {
        setOnDismissListener {
            callback()
        }
        titleView.text = title
        descriptionView.text = description
        dismissButton.setOnClickListener { dismiss() }
    }
}