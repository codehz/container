package one.codehz.container

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import one.codehz.container.base.BaseActivity
import one.codehz.container.ext.get
import one.codehz.container.ext.staticName
import one.codehz.container.ext.transaction
import one.codehz.container.ext.vUserManager
import one.codehz.container.fragment.PreferenceFragmentImpl
import one.codehz.container.provider.PreferenceProvider

class UserSettingActivity : BaseActivity(R.layout.user_settings) {
    companion object {
        val EXTRA_USER_ID by staticName
    }

    val userId by lazy { intent.getIntExtra(EXTRA_USER_ID, 0) }
    var userName
        get() = vUserManager.users.find { it.id == userId }?.name ?: throw IllegalStateException()
        set(value) = vUserManager.setUserName(userId, value)

    val userNameEdit by lazy<EditText> { this[R.id.name_edit] }

    val fragmentImpl by lazy {
        PreferenceFragmentImpl(
                R.xml.user_preference,
                PreferenceProvider.PRE_USER_URI,
                "key",
                "value",
                mapOf("user" to userId.toString()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userNameEdit.setText(userName, TextView.BufferType.EDITABLE)
        userNameEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                userName = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        })

        supportFragmentManager.transaction { replace(R.id.frame, fragmentImpl) }
    }
}