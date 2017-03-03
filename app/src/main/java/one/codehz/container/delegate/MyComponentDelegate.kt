package one.codehz.container.delegate

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import com.lody.virtual.client.hook.delegate.ComponentDelegate
import com.lody.virtual.helper.utils.VLog
import one.codehz.container.ext.vClientImpl
import one.codehz.container.provider.MainProvider
import one.codehz.container.provider.PreferenceProvider
import one.codehz.container.utils.DatabasePreferences


class MyComponentDelegate(val context: Context) : ComponentDelegate {
    val prePackage = mutableMapOf<String, DatabasePreferences>()

    fun acquirePrePackagePreferences(pkgName: String): DatabasePreferences {
        if (pkgName in prePackage) return prePackage[pkgName] ?: throw AssertionError()
        prePackage[pkgName] = DatabasePreferences(context, PreferenceProvider.PRE_PACKAGE_URI, "key", "value", mapOf("package" to pkgName))
        return prePackage[pkgName] ?: throw AssertionError()
    }

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

    override fun onSetForeground(pkgName: String) = acquirePrePackagePreferences(pkgName).getBoolean("foreground_service_notification", false)

    override fun onStartService(intent: Intent?): Boolean {
        if (intent?.component != null)
            return logComponent("service", intent.component?.className!!)
        return true
    }

    override fun onSendBroadcast(intent: Intent?): Boolean {
        if (intent?.action != null)
            return logComponent("broadcast", intent.action!!)
        return true
    }

    override fun onAcquireContentProvider(name: String?): Boolean {
        if (name != null && name != "one.codehz.container.provider.main")
            return logComponent("provider", name)
        return true
    }
}