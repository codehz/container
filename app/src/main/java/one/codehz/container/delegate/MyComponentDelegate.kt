package one.codehz.container.delegate

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.lody.virtual.client.hook.delegate.ComponentDelegate
import com.lody.virtual.helper.utils.VLog
import one.codehz.container.provider.MainProvider


class MyComponentDelegate(val context: Context) : ComponentDelegate {
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

    override fun onSendBroadcast(intent: Intent?):Boolean {
        VLog.d("MyComponentDelegate", "onSendBroadcast %s", intent.toString())
        if (intent?.action != null) {
            val action = intent?.action!!
            context.contentResolver.query(MainProvider.URI_BUILDER.appendPath("component").build(), arrayOf("regex"), "`type`=\"broadcast\"", null, null).use {
                if (Regex.fromLiteral(it.getString(0)).matches(action))
                    return false
            }
        }
        return true
    }
}