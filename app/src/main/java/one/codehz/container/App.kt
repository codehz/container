package one.codehz.container

import android.app.Application
import android.content.Context
import android.os.Build
import com.lody.virtual.client.stub.StubManifest
import mirror.RefStaticObject
import one.codehz.container.delegate.*
import one.codehz.container.ext.sharedPreferences
import one.codehz.container.ext.virtualCore

class App : Application() {

    companion object {
        private var app: App? = null

        val self: App
            get() = app!!
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        StubManifest.ENABLE_IO_REDIRECT = true
        virtualCore.startup(base)
    }

    fun RefStaticObject<String>?.setIfNotEmpty(value: String) = if (value.isNotEmpty()) this?.set(value) else Unit

    override fun onCreate() {
        super.onCreate()
        with(virtualCore) {
            when {
                isVAppProcess -> {
                    componentDelegate = MyComponentDelegate()
                    phoneInfoDelegate = MyPhoneInfoDelegate()
                    taskDescriptionDelegate = MyTaskDescriptionDelegate()
                    ioRedirectDelegate = MyIORedirectDelegate()

                    mirror.android.os.Build.MODEL.setIfNotEmpty(sharedPreferences.getString("privacy_device_model", ""))
                    mirror.android.os.Build.MANUFACTURER.setIfNotEmpty(sharedPreferences.getString("privacy_device_manufacturer", ""))
                    mirror.android.os.Build.BRAND.setIfNotEmpty(sharedPreferences.getString("privacy_device_brand", ""))
                    mirror.android.os.Build.PRODUCT.setIfNotEmpty(sharedPreferences.getString("privacy_device_product", ""))
                    mirror.android.os.Build.DEVICE.setIfNotEmpty(sharedPreferences.getString("privacy_device_device", ""))
                    Unit
                }
                isServerProcess -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        foregroundNotificationDelegate = MyForegroundNotificationDelegate(this@App)
                }
                else -> {
                    setAppRequestListener(MyAppRequestListener())
                }
            }
        }
    }
}