package one.codehz.container.delegate

import com.lody.virtual.client.hook.delegate.PhoneInfoDelegate
import one.codehz.container.ext.sharedPreferences

class MyPhoneInfoDelegate : PhoneInfoDelegate {
    override fun getDeviceId(oldDeviceId: String): String {
        sharedPreferences.getString("privacy_phone_device_id", "").apply {
            if (isNotEmpty())
                return this
        }
        return oldDeviceId
    }

    override fun getBluetoothAddress(oldAddress: String): String {
        sharedPreferences.getString("privacy_phone_bluetooth_address", "").apply {
            if (isNotEmpty())
                return this
        }
        return oldAddress
    }

    override fun getMacAddress(oldAddress: String?): String? {
        sharedPreferences.getString("privacy_phone_mac_address", "").apply {
            if (isNotEmpty())
                return this
        }
        return oldAddress
    }

    override fun getLine1Number(oldNumber: String?): String? {
        sharedPreferences.getString("privacy_phone_number", "").apply {
            if (isNotEmpty())
                return this
        }
        return oldNumber
    }
}