package one.codehz.container.fragment

import android.os.Bundle
import android.preference.PreferenceFragment
import one.codehz.container.MainActivity
import one.codehz.container.R
import one.codehz.container.interfaces.IFloatingActionTarget

class SettingFragment : PreferenceFragment(), IFloatingActionTarget {
    override val canBeFloatingActionTarget = false

    override fun onFloatingAction() = throw UnsupportedOperationException("not implemented")

    override fun getFloatingDrawable() = throw UnsupportedOperationException("not implemented")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.preferences)
    }

    override fun onResume() {
        super.onResume()

        (activity as MainActivity).setTask(R.string.task_setting)
    }
}