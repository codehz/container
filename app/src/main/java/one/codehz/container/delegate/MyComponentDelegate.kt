package one.codehz.container.delegate

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import com.lody.virtual.client.hook.delegate.ComponentDelegate
import com.lody.virtual.helper.utils.VLog
import one.codehz.container.ext.vClientImpl
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

    fun checkComponent(type: String, action: String): Boolean {
        context.contentResolver.query(MainProvider.COMPONENT_URI, arrayOf("action"), "`type`=\"$type\" AND package=\"${vClientImpl.currentPackage}\"", null, null).use {
            generateSequence { if (it.moveToNext()) it else null }.map { it.getString(0) }.forEach {
                VLog.d("MCD", "%s == %s", it, action)
                if (it == action)
                    return false
            }
        }
        return true
    }

    fun logComponent(type: String, action: String) = checkComponent(type, action).apply {
        val result = if (this) 1 else 0
        context.contentResolver.insert(MainProvider.COMPONENT_LOG_URI, ContentValues().apply {
            put("package", vClientImpl.currentPackage)
            put("type", type)
            put("action", action)
            put("result", result)
        })
    }

    override fun onSendBroadcast(intent: Intent?): Boolean {
        VLog.d("MyComponentDelegate", "onSendBroadcast %s", intent.toString())
        if (intent?.action != null)
            return logComponent("broadcast", intent?.action!!)
        return true
    }
}