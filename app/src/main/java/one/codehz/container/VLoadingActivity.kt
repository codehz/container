package one.codehz.container

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import one.codehz.container.ext.get
import one.codehz.container.ext.vActivityManager
import one.codehz.container.ext.virtualCore
import one.codehz.container.models.AppModel

class VLoadingActivity : Activity() {
    val iconView by lazy<ImageView> { this[R.id.icon] }
    val titleView by lazy<TextView> { this[R.id.title] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loading_page)

        val package_name = intent.data.path.substring(1)
        val userId = intent.data.fragment.toInt()
        val model = AppModel(this, virtualCore.findApp(package_name))
        iconView.setImageDrawable(model.icon)
        titleView.text = model.name

        val target = virtualCore.getLaunchIntent(package_name, userId)

        virtualCore.setLoadingPage(target, this)

        if (intent.data.getQueryParameter("delay") != null)
            Handler().postDelayed({
                vActivityManager.startActivity(target, userId)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }, 50)
        else
            vActivityManager.startActivity(target, userId)
    }
}