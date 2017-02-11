package one.codehz.container.widget

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.TextView
import one.codehz.container.R
import one.codehz.container.ext.get
import one.codehz.container.ext.virtualCore
import one.codehz.container.models.AppModel

class OptimizingDialog(context: Context) : Dialog(context) {
    init {
        setContentView(R.layout.opt_dialog)
    }
    val iconView by lazy<ImageView> { this[R.id.icon] }
    val titleView by lazy<TextView> { this[R.id.title] }

    var name: CharSequence
        get() = titleView.text
        set(value) {
            titleView.text = value
        }

    var icon: Drawable
        get() = iconView.drawable
        set(value) {
            iconView.setImageDrawable(value)
        }

    class OptimizeHelper(val appModel: AppModel, val ui: (() -> Unit) -> Unit) {
        val dialog by lazy { OptimizingDialog(appModel.context) }

        fun optimize() {
            ui {
                dialog.apply {
                    name = appModel.name
                    icon = appModel.icon
                    show()
                }
            }
            virtualCore.preOpt(appModel.packageName)
            ui {
                dialog.dismiss()
            }
        }
    }
}