package one.codehz.container.delegate

import android.annotation.SuppressLint
import android.os.Environment
import com.lody.virtual.client.hook.delegate.IORedirectDelegate

class MyIORedirectDelegate : IORedirectDelegate {
    @SuppressLint("SdCardPath")
    override fun getIORedirect(): Map<String, String> {
        val mediaDirs = Environment.getExternalStorageDirectory().absolutePath
        return mapOf(
                "/sdcard" to "/sdcard/virtual",
                "/mnt/sdcard" to "/mnt/sdcard/virtual",
                mediaDirs to "$mediaDirs/virtual")
    }

    @SuppressLint("SdCardPath")
    override fun getIOReversedRedirect(): Map<String, String> {
        return ioRedirect.map { it.value + "/reversed" to it.key }.toMap()
    }
}