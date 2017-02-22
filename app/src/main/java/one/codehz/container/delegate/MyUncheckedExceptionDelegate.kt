package one.codehz.container.delegate

import android.content.ContentValues
import com.lody.virtual.client.hook.delegate.UncheckedExceptionDelegate
import one.codehz.container.ext.vClientImpl
import one.codehz.container.ext.virtualCore
import one.codehz.container.provider.MainProvider
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class MyUncheckedExceptionDelegate : UncheckedExceptionDelegate {
    val logRoot by lazy { File(virtualCore.context.getExternalFilesDir(""), "/log/${vClientImpl.currentPackage}").apply { if (!exists()) mkdirs(); } }
    val logUncaught by lazy { File(logRoot, "/uncaught.txt").apply { if (!exists()) createNewFile() } }
    val logShutdown by lazy { File(logRoot, "/shutdown.txt").apply { if (!exists()) createNewFile() } }

    override fun onThreadGroupUncaughtException(t: Thread, e: Throwable) {
        logUncaught.printWriter(Charsets.UTF_8).use {
            e.printStackTrace(it)
        }
        virtualCore.context.contentResolver.insert(MainProvider.URI_BUILDER.appendPath("log").build(), ContentValues().apply {
            put("package", vClientImpl.currentPackage)
            put("data", StringWriter().apply { e.printStackTrace(PrintWriter(this)) }.toString())
        })
    }

    override fun onShutdown(e: Throwable) {
        logShutdown.printWriter(Charsets.UTF_8).use {
            e.printStackTrace(it)
        }
    }
}