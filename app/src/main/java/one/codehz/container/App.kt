package one.codehz.container

import android.app.Application
import android.content.Context
import android.os.Build
import com.lody.virtual.client.stub.StubManifest
import one.codehz.container.delegate.*
import one.codehz.container.ext.virtualCore

class App : Application() {

    companion object {
        private var app: App? = null

        val one: App
            get() = app!!
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        StubManifest.ENABLE_IO_REDIRECT = true
        virtualCore.startup(base)
    }

    override fun onCreate() {
        super.onCreate()
        with(virtualCore) {
            when {
                isVAppProcess -> {
                    componentDelegate = MyComponentDelegate()
                    phoneInfoDelegate = MyPhoneInfoDelegate()
                    taskDescriptionDelegate = MyTaskDescriptionDelegate()
                    ioRedirectDelegate = MyIORedirectDelegate()
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