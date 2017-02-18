package one.codehz.container.delegate

import com.lody.virtual.client.hook.delegate.UnckeckedExceptionDelegate
import one.codehz.container.ext.vClientImpl
import one.codehz.container.ext.virtualCore
import java.io.File

class MyUnckeckedExceptionDelegate : UnckeckedExceptionDelegate {
    init {
        File(virtualCore.context.externalMediaDirs[0], "/log/${vClientImpl.currentPackage}").mkdirs()
        File(virtualCore.context.externalMediaDirs[0], "/log/${vClientImpl.currentPackage}/uncaught.txt").createNewFile()
        File(virtualCore.context.externalMediaDirs[0], "/log/${vClientImpl.currentPackage}/onshutdown.txt").createNewFile()
    }

    override fun onThreadGroupUncaughtException(t: Thread, e: Throwable) {
        File(virtualCore.context.externalMediaDirs[0], "/log/${vClientImpl.currentPackage}/uncaught.txt").printWriter(Charsets.UTF_8).use {
            println(e)
        }
    }

    override fun onShutdown(e: Throwable) {
        File(virtualCore.context.externalMediaDirs[0], "/log/${vClientImpl.currentPackage}/onshutdown.txt").printWriter(Charsets.UTF_8).use {
            println(e)
        }
    }
}