package one.codehz.container.widget

import android.content.Context
import android.graphics.Rect
import android.support.design.widget.BottomSheetDialog
import android.view.WindowManager
import one.codehz.container.R


open class BaseBottomSheetDialog(context: Context, layoutResId: Int = 0) : BottomSheetDialog(context, R.style.MyBottomSheetDialog) {
    init {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (layoutResId != 0) super.setContentView(layoutResId)
    }
}