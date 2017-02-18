package one.codehz.container.delegate

import com.lody.virtual.client.hook.delegate.UncheckedExceptionDelegate
import one.codehz.container.ext.vClientImpl
import one.codehz.container.ext.virtualCore
import java.io.File

class MyUncheckedExceptionDelegate : UncheckedExceptionDelegate {
    val logRoot by lazy { File(virtualCore.context.getExternalFilesDir(""), "/log/${vClientImpl.currentPackage}").apply { if (!exists()) mkdirs(); } }
    val logUncaught by lazy { File(logRoot, "/uncaught.txt").apply { if (!exists()) createNewFile() } }
    val logShutdown by lazy { File(logRoot, "/shutdown.txt").apply { if (!exists()) createNewFile() } }

    override fun onThreadGroupUncaughtException(t: Thread, e: Throwable) {
        logUncaught.printWriter(Charsets.UTF_8).use {
            e.printStackTrace(it)
        }
    }

    override fun onShutdown(e: Throwable) {
        logShutdown.printWriter(Charsets.UTF_8).use {
            e.printStackTrace(it)
        }
    }
}