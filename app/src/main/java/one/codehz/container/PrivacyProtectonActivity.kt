package one.codehz.container

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceFragmentCompat
import one.codehz.container.ext.transaction

class PrivacyProtectonActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.transaction { replace(android.R.id.content, FakeModelFragment()) }
    }

    class FakeModelFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?)
                = addPreferencesFromResource(R.xml.privacy_model)
    }
}