package one.codehz.container

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.transition.Fade
import android.transition.Transition
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.lody.virtual.client.ipc.VActivityManager
import com.lody.virtual.os.VUserHandle
import one.codehz.container.ext.*
import one.codehz.container.models.AppModel

class LoadingActivity : Activity() {
    companion object {
        fun launch(context: Activity, appModel: AppModel, userId: Int, iconView: View, nameView: View) {
            context.startActivity(Intent(context, LoadingActivity::class.java).apply {
                action = Intent.ACTION_RUN
                data = Uri.Builder().scheme("container").authority("launch").appendPath(appModel.packageName).fragment(userId.toString()).build()
            }, ActivityOptions.makeSceneTransitionAnimation(context, iconView pair "app_icon", nameView pair "app_name").toBundle())
        }

        fun launch(context: Activity, appModel: AppModel, userId: Int, iconView: View) {
            context.startActivity(Intent(context, LoadingActivity::class.java).apply {
                action = Intent.ACTION_RUN
                data = Uri.Builder().scheme("container").authority("launch").appendPath(appModel.packageName).fragment(userId.toString()).build()
            }, ActivityOptions.makeSceneTransitionAnimation(context, iconView, "app_icon").toBundle())
        }
    }

    val iconView by lazy<ImageView> { this[R.id.icon] }
    val titleView by lazy<TextView> { this[R.id.title] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.loading_page)

        val package_name = intent.data.path.substring(1)
        val model = AppModel(this, virtualCore.findApp(package_name))
        iconView.setImageDrawable(model.icon)
        titleView.text = model.name

        window.sharedElementEnterTransition.addListener(object : Transition.TransitionListener {
            override fun onTransitionEnd(transition: Transition?) {
                val delay = intent.data.buildUpon().appendQueryParameter("delay", "true").build()
                runAsync<Unit, Unit> { unit ->
                    virtualCore.preOpt(package_name)
                }.then { unit ->
                    if (android.os.Build.VERSION.SDK_INT >= 24 && isInMultiWindowMode) {
                        startActivity(Intent(this@LoadingActivity, VLoadingActivity::class.java).apply {
                            action = Intent.ACTION_RUN
                            data = delay
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
                        })
                    } else {
                        startActivity(Intent(this@LoadingActivity, VLoadingActivity::class.java).apply {
                            action = Intent.ACTION_RUN
                            data = delay
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    }
                    overridePendingTransition(0, android.R.anim.fade_out)
                    finish()
                }.execute(Unit)
            }

            override fun onTransitionResume(transition: Transition?) = Unit
            override fun onTransitionPause(transition: Transition?) = Unit
            override fun onTransitionCancel(transition: Transition?) = Unit
            override fun onTransitionStart(transition: Transition?) = Unit
        })
    }
}