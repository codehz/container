package one.codehz.container.delegate

import android.content.Context
import android.content.SharedPreferences
import com.lody.virtual.client.hook.delegate.PhoneInfoDelegate
import one.codehz.container.ext.sharedPreferences
import one.codehz.container.provider.PreferenceProvider
import one.codehz.container.utils.DatabasePreferences

class MyPhoneInfoDelegate(val context: Context) : PhoneInfoDelegate {
    val preferencesMap = emptyMap<Int, DatabasePreferences>().toMutableMap()

    fun getPreference(userId: Int): SharedPreferences {
        if (userId !in preferencesMap)
            preferencesMap[userId] = DatabasePreferences(context, PreferenceProvider.PRE_USER_URI, "key", "value", mapOf("user" to userId.toString()))
        return preferencesMap[userId] ?: throw IllegalStateException()
    }

    override fun getDeviceId(oldDeviceId: String, userId: Int): String {
        getPreference(userId).getString("privacy_phone_device_id", "").apply {
            if (isNotEmpty())
                return this
            else
                sharedPreferences.getString("privacy_phone_device_id", "").apply {
                    if (isNotEmpty())
                        return this
                }
        }
        return oldDeviceId
    }

    override fun getBluetoothAddress(oldAddress: String, userId: Int): String {
        getPreference(userId).getString("privacy_phone_bluetooth_address", "").apply {
            if (isNotEmpty())
                return this
            else
                sharedPreferences.getString("privacy_phone_bluetooth_address", "").apply {
                    if (isNotEmpty())
                        return this
                }
        }
        return oldAddress
    }

    override fun getMacAddress(oldAddress: String?, userId: Int): String? {
        getPreference(userId).getString("privacy_phone_mac_address", "").apply {
            if (isNotEmpty())
                return this
            else
                sharedPreferences.getString("privacy_phone_mac_address", "").apply {
                    if (isNotEmpty())
                        return this
                }
        }
        return oldAddress
    }

    override fun getLine1Number(oldNumber: String?, userId: Int): String? {
        getPreference(userId).getString("privacy_phone_number", "").apply {
            if (isNotEmpty())
                return this
            else
                sharedPreferences.getString("privacy_phone_number", "").apply {
                    if (isNotEmpty())
                        return this
                }
        }
        return oldNumber
    }
}