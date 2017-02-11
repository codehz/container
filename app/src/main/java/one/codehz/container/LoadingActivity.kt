package one.codehz.container

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
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
        val KEY_PACKAGE_NAME by staticName
        val KEY_USER_ID by staticName

        fun launch(context: Activity, appModel: AppModel, userId: Int, iconView: View, nameView: View) {
            virtualCore.getLaunchIntent(appModel.packageName, userId)?.let {
                context.startActivity(Intent(context, LoadingActivity::class.java).apply {
                    putExtra(KEY_PACKAGE_NAME, appModel.packageName)
                    putExtra(KEY_USER_ID, userId)
                }, ActivityOptions.makeSceneTransitionAnimation(context, iconView pair "app_icon", nameView pair "app_name").toBundle())
            }
        }

        fun launch(context: Activity, appModel: AppModel, userId: Int, iconView: View) {
            virtualCore.getLaunchIntent(appModel.packageName, userId)?.let {
                context.startActivity(Intent(context, LoadingActivity::class.java).apply {
                    putExtra(KEY_PACKAGE_NAME, appModel.packageName)
                    putExtra(KEY_USER_ID, userId)
                }, ActivityOptions.makeSceneTransitionAnimation(context, iconView, "app_icon").toBundle())
            }
        }
    }

    val iconView by lazy<ImageView> { this[R.id.icon] }
    val titleView by lazy<TextView> { this[R.id.title] }
    val handler by lazy { Handler() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.loading_page)

        val package_name = intent.getStringExtra(KEY_PACKAGE_NAME)
        val userId = intent.getIntExtra(KEY_USER_ID, 0)
        val model = AppModel(this, virtualCore.findApp(package_name))
        iconView.setImageDrawable(model.icon)
        titleView.text = model.name

        window.sharedElementEnterTransition.addListener(object : Transition.TransitionListener {
            override fun onTransitionEnd(transition: Transition?) {
                runAsync<Unit, Unit> { unit ->
                    virtualCore.preOpt(package_name)
                }.then { unit ->
                    if (android.os.Build.VERSION.SDK_INT >= 24 && isInMultiWindowMode) {
                        startActivity(Intent(this@LoadingActivity, VLoadingActivity::class.java).apply {
                            putExtra(VLoadingActivity.KEY_PACKAGE_NAME, package_name)
                            putExtra(VLoadingActivity.KEY_USER_ID, userId)
                            putExtra(VLoadingActivity.KEY_INTENT, virtualCore.getLaunchIntent(model.packageName, VUserHandle.USER_OWNER))
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
                        })
                    } else {
                        startActivity(Intent(this@LoadingActivity, VLoadingActivity::class.java).apply {
                            putExtra(VLoadingActivity.KEY_PACKAGE_NAME, package_name)
                            putExtra(VLoadingActivity.KEY_USER_ID, userId)
                            putExtra(VLoadingActivity.KEY_INTENT, virtualCore.getLaunchIntent(model.packageName, VUserHandle.USER_OWNER))
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