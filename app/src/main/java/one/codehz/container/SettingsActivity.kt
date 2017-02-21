package one.codehz.container

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.preference.PreferenceGroup
import one.codehz.container.ext.sharedPreferences
import one.codehz.container.ext.transaction

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = resources.getIdentifier(intent.data.authority, "xml", "one.codehz.container")

        supportFragmentManager.transaction { replace(android.R.id.content, FakeModelFragment(id)) }
    }

    class FakeModelFragment(val xml: Int) : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?)
                = addPreferencesFromResource(xml)

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            (0..preferenceScreen.preferenceCount - 1).map { preferenceScreen.getPreference(it) }.mapNotNull { it as? PreferenceGroup }.forEach { category ->
                (0..category.preferenceCount - 1).map { category.getPreference(it) }.mapNotNull { it as? EditTextPreference }.forEach { it.summary = it.text }
            }
            sharedPreferences.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
                findPreference(key)?.apply {
                    if (this is EditTextPreference) {
                        summary = text.toString()
                    }
                }
            }
        }
    }
}