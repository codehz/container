package one.codehz.container

import android.Manifest
import android.app.ActivityManager
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.transition.Transition
import android.view.MenuItem
import one.codehz.container.base.BaseActivity
import one.codehz.container.ext.get
import one.codehz.container.ext.transaction
import one.codehz.container.fragment.InstalledFragment
import one.codehz.container.fragment.RunningFragment
import one.codehz.container.fragment.SettingFragment
import one.codehz.container.interfaces.IFloatingActionTarget
import java.security.InvalidParameterException

class MainActivity : BaseActivity(R.layout.activity_main) {

    val fab by lazy<FloatingActionButton> { this[R.id.fab] }
    val bottomNavigationView by lazy<BottomNavigationView> { this[R.id.bottom_navigation] }
    val handler by lazy { Handler() }
    var isTransition = false
    val currentFragment: IFloatingActionTarget
        get() = supportFragmentManager.findFragmentByTag("current") as IFloatingActionTarget

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()

        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.M)
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)

        hookTransitions()

        handler
    }

    fun setTask(task: Int) {
        title = getString(R.string.task_container_prefix, getString(task))
        setTaskDescription(ActivityManager.TaskDescription(
                title.toString(),
                (getDrawable(R.mipmap.ic_launcher) as BitmapDrawable).bitmap,
                ContextCompat.getColor(this, R.color.colorPrimaryDark)))
    }

    private fun hookTransitions() {
        window.exitTransition.addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition?) {
                isTransition = true
            }

            override fun onTransitionEnd(transition: Transition?) {
                isTransition = false
            }

            override fun onTransitionResume(transition: Transition?) = Unit
            override fun onTransitionPause(transition: Transition?) = Unit
            override fun onTransitionCancel(transition: Transition?) = Unit
        })
    }

    fun initViews() {
        fab.setOnClickListener { view ->
            currentFragment.onFloatingAction()
        }

        bottomNavigationView.setOnNavigationItemSelectedListener {
            selectFragment(it)
            true
        }

        if (intent.action == "one.codehz.container.TAB_RUNNING")
            selectFragment(bottomNavigationView.menu.findItem(R.id.running))
        else
            selectFragment(bottomNavigationView.menu.findItem(R.id.installed))
    }

    fun selectFragment(menuItem: MenuItem) {
        menuItem.isChecked = true

        val frag: Fragment = when (menuItem.itemId) {
            R.id.installed -> InstalledFragment()
            R.id.running -> RunningFragment()
            R.id.settings -> SettingFragment()
            else -> throw InvalidParameterException()
        }

        if ((frag as IFloatingActionTarget).canBeFloatingActionTarget) {
            fab.show()
            fab.setImageDrawable(ContextCompat.getDrawable(this, frag.getFloatingDrawable()))
        } else
            fab.hide()

        supportFragmentManager.transaction { replace(R.id.frame, frag, "current") }
    }
}
