package one.codehz.container.delegate

import android.util.Log
import com.lody.virtual.client.hook.delegate.PhoneInfoDelegate

class MyPhoneInfoDelegate : PhoneInfoDelegate {
    override fun getDeviceId(oldDeviceId: String): String {
        Log.d("MyPhoneInfoDelegate", "WANT GET DEVICE ID $oldDeviceId")
        return (oldDeviceId.toLong() + 1).toString()
    }

    override fun getBluetoothAddress(oldAddress: String) = oldAddress
}