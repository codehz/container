package one.codehz.container.fragment

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import one.codehz.container.MainActivity
import one.codehz.container.R
import one.codehz.container.interfaces.IFloatingActionTarget

class SettingFragment : PreferenceFragmentCompat(), IFloatingActionTarget {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }

    override val canBeFloatingActionTarget = false

    override fun onFloatingAction() = throw UnsupportedOperationException("not implemented")

    override fun getFloatingDrawable() = throw UnsupportedOperationException("not implemented")

    override fun onResume() {
        super.onResume()

        (activity as MainActivity).setTask(R.string.task_setting)
    }
}