package one.codehz.container.fragment

import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.PreferenceGroup
import com.lody.virtual.helper.utils.Reflect
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat
import one.codehz.container.ext.sharedPreferences
import one.codehz.container.utils.DatabasePreferences

class PreferenceFragmentImpl(val res: Int, val uri: Uri, val keyName: String, val valueName: String, val staticField: Map<String, String>) : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        findPreference(key)?.apply {
            if (this is EditTextPreference) {
                summary = text.toString()
            }
        }
    }

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        Reflect.on(this.preferenceManager).set("mSharedPreferences", DatabasePreferences(context, uri, keyName, valueName, staticField))
        addPreferencesFromResource(res)
        (0..preferenceScreen.preferenceCount - 1).map { preferenceScreen.getPreference(it) }.mapNotNull { it as? PreferenceGroup }.forEach { category ->
            (0..category.preferenceCount - 1).map { category.getPreference(it) }.mapNotNull { it as? EditTextPreference }.forEach { it.summary = it.text }
        }
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}