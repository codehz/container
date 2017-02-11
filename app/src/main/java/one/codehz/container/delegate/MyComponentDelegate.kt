package one.codehz.container.delegate

import android.app.Activity
import android.content.Intent
import com.lody.virtual.client.hook.delegate.ComponentDelegate


class MyComponentDelegate : ComponentDelegate {
    override fun beforeActivityCreate(activity: Activity?) {
    }

    override fun beforeActivityResume(activity: Activity?) {
    }

    override fun beforeActivityPause(activity: Activity?) {
    }

    override fun beforeActivityDestroy(activity: Activity?) {
    }

    override fun afterActivityCreate(activity: Activity?) {
    }

    override fun afterActivityResume(activity: Activity?) {
    }

    override fun afterActivityPause(activity: Activity?) {
    }

    override fun afterActivityDestroy(activity: Activity?) {
    }

    override fun onSendBroadcast(intent: Intent?) {
    }
}