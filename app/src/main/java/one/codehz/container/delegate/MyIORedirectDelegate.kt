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

    override fun getIOReversedRedirect() = ioRedirect.map { it.value + "/reversed" to it.key }.toMap()

    override fun getContentReversedRedirect() = ioRedirect.map { it.key to it.key + "/reversed" }.toMap()
}