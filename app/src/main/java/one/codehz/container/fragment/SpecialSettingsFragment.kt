package one.codehz.container.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import one.codehz.container.R
import one.codehz.container.ext.transaction
import one.codehz.container.models.AppModel
import one.codehz.container.provider.PreferenceProvider

class SpecialSettingsFragment(val model: AppModel) : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            = inflater.inflate(R.layout.special_settings, container, false)!!

    val fragmentImpl by lazy {
        PreferenceFragmentImpl(
                R.xml.special_preference,
                PreferenceProvider.PRE_PACKAGE_URI,
                "key",
                "value",
                mapOf("package" to model.packageName)
        )
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentManager.transaction { replace(R.id.frame, fragmentImpl) }
    }
}