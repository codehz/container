package one.codehz.container.behavior

import android.app.ProgressDialog.show
import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.util.AttributeSet
import android.util.Log
import android.view.View
import one.codehz.container.MainActivity

@Suppress("unused")
class ScrollAwareFABBehavior(context: Context, attrs: AttributeSet) : FloatingActionButton.Behavior(context, attrs) {
    override fun layoutDependsOn(parent: CoordinatorLayout?, child: FloatingActionButton, dependency: View) = when (dependency) {
        is BottomNavigationView -> true
        else -> false
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: FloatingActionButton, dependency: View): Boolean {
        if ((dependency.context as? MainActivity)?.isTransition ?: false) {
            if (child.visibility == View.VISIBLE) child.visibility = View.GONE
            return false
        }
        return when (dependency) {
            is BottomNavigationView -> {
                with(child) {
                    val enabled = (dependency.context as MainActivity).currentFragment.canBeFloatingActionTarget
                    when {
                        visibility == View.VISIBLE && (dependency.translationY > dependency.height / 2 || !enabled) -> hide()
                        visibility != View.VISIBLE && dependency.translationY == 0f && enabled -> show()
                    }
                }
                true
            }
            else -> super.onDependentViewChanged(parent, child, dependency)
        }
    }
}