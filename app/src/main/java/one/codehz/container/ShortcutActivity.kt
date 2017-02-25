package one.codehz.container

import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import one.codehz.container.ext.activityManager
import one.codehz.container.ext.virtualCore

class ShortcutActivity : Activity() {
    companion object {
        val CLEAR_ALL = "one.codehz.container.CLEAR_ALL"
        val SELF_DETAIL = "one.codehz.container.SELF_DETAIL"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (intent.action) {
            CLEAR_ALL -> {
                virtualCore.killAllApps()
                activityManager.appTasks
                        .filter { it.taskInfo.baseIntent.component.className.startsWith("com.lody.virtual.client.stub.StubActivity$") }
                        .forEach(ActivityManager.AppTask::finishAndRemoveTask)
            }
            SELF_DETAIL -> {
                startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                })
            }
        }

        finish()
    }
}