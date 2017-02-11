package one.codehz.container.delegate

import android.annotation.SuppressLint
import android.os.Environment
import com.lody.virtual.client.hook.delegate.IORedirectDelegate

class MyIORedirectDelegate : IORedirectDelegate {
    @SuppressLint("SdCardPath")
    override fun getIORedirect(): MutableMap<String, String> {
        val mediaDirs = Environment.getExternalStorageDirectory().absolutePath
        return mutableMapOf(
                "/sdcard" to "/sdcard/virtual",
                "/mnt/sdcard" to "/mnt/sdcard/virtual",
                mediaDirs to "$mediaDirs/virtual")
    }
}