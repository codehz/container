package one.codehz.container

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import android.widget.TextView
import com.lody.virtual.os.VUserHandle
import one.codehz.container.ext.get
import one.codehz.container.ext.staticName
import one.codehz.container.ext.vActivityManager
import one.codehz.container.ext.virtualCore
import one.codehz.container.models.AppModel

class VLoadingActivity : Activity() {
    companion object {
        val KEY_PACKAGE_NAME by staticName
        val KEY_USER_ID by staticName
        val KEY_INTENT by staticName
    }

    val iconView by lazy<ImageView> { this[R.id.icon] }
    val titleView by lazy<TextView> { this[R.id.title] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loading_page)

        val package_name = intent.getStringExtra(KEY_PACKAGE_NAME)
        val userId = intent.getIntExtra(LoadingActivity.KEY_USER_ID, 0)
        val model = AppModel(this, virtualCore.findApp(package_name))
        iconView.setImageDrawable(model.icon)
        titleView.text = model.name

        val target = intent.getParcelableExtra<Intent>(KEY_INTENT)
        virtualCore.setLoadingPage(target, this)

        Handler().postDelayed({
            vActivityManager.startActivity(target, userId)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, 50)
    }
}