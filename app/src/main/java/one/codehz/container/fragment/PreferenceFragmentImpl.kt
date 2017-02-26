package one.codehz.container.fragment

import android.net.Uri
import android.os.Bundle
import com.lody.virtual.helper.utils.Reflect
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat
import one.codehz.container.utils.DatabasePreferences

class PreferenceFragmentImpl(val res: Int, val uri: Uri, val keyName: String, val valueName: String, val staticField: Map<String, String>) : PreferenceFragmentCompat() {
    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        Reflect.on(this.preferenceManager).set("mSharedPreferences", DatabasePreferences(context, uri, keyName, valueName, staticField))
        addPreferencesFromResource(res)
    }
}