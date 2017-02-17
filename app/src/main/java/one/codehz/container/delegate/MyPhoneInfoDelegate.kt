package one.codehz.container.delegate

import android.util.Log
import com.lody.virtual.client.hook.delegate.PhoneInfoDelegate
import one.codehz.container.ext.sharedPreferences

class MyPhoneInfoDelegate : PhoneInfoDelegate {
    override fun getDeviceId(oldDeviceId: String): String {
        Log.d("MyPhoneInfoDelegate", "WANT GET DEVICE ID $oldDeviceId")
        sharedPreferences.getString("privacy_phone_device_id", "").apply {
            if (isNotEmpty())
                return this
        }
        return (oldDeviceId.toLong() + 1).toString()
    }

    override fun getBluetoothAddress(oldAddress: String): String {
        sharedPreferences.getString("privacy_phone_bluetooth_address", "").apply {
            if (isNotEmpty())
                return this
        }
        return oldAddress
    }
}