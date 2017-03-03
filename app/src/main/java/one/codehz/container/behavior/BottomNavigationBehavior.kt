package one.codehz.container.behavior

import android.content.Context
import android.graphics.Rect
import android.support.design.widget.AppBarLayout
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.view.WindowInsetsCompat
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import one.codehz.container.MainActivity

@Suppress("unused")
class BottomNavigationBehavior(context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<BottomNavigationView>(context, attrs) {
    override fun onApplyWindowInsets(coordinatorLayout: CoordinatorLayout?, child: BottomNavigationView?, insets: WindowInsetsCompat): WindowInsetsCompat {
        Log.d("BMB", insets.toString())
        return super.onApplyWindowInsets(coordinatorLayout, child, insets)
    }

    fun adjustSnackBarLayout(dependency: Snackbar.SnackbarLayout, child: BottomNavigationView) = dependency.apply { setPadding(paddingLeft, paddingTop, paddingRight, (child.height - child.translationY).toInt()) }

    override fun layoutDependsOn(parent: CoordinatorLayout?, child: BottomNavigationView, dependency: View): Boolean {
        if ((dependency.context as? MainActivity)?.isTransition ?: false) return false
        return when (dependency) {
            is AppBarLayout -> true
            is Snackbar.SnackbarLayout -> {
                adjustSnackBarLayout(dependency, child)
                true
            }
            else -> super.layoutDependsOn(parent, child, dependency)
        }
    }

    var modified = false

    override fun onDependentViewChanged(parent: CoordinatorLayout?, child: BottomNavigationView, dependency: View): Boolean {
        if ((dependency.context as? MainActivity)?.isTransition ?: false) return false
        return when (dependency) {
            is AppBarLayout -> {
                val value = child.context.resources.getDimensionPixelSize(child.context.resources.getIdentifier("status_bar_height", "dimen", "android")) - dependency.y
                child.translationY = value / dependency.height * child.height
                modified = true
                true
            }
            is Snackbar.SnackbarLayout -> {
                if (modified)
                    adjustSnackBarLayout(dependency, child)
                modified = false
                true
            }
            else -> false
        }
    }

    override fun onAttachedToLayoutParams(params: CoordinatorLayout.LayoutParams) {
        params.insetEdge = Gravity.BOTTOM
        super.onAttachedToLayoutParams(params)
    }

    override fun getInsetDodgeRect(parent: CoordinatorLayout, child: BottomNavigationView, rect: Rect): Boolean {
        Log.d("BMB", "Inset Doge Rect $rect")
        return super.getInsetDodgeRect(parent, child, rect)
    }

    override fun onRequestChildRectangleOnScreen(coordinatorLayout: CoordinatorLayout?, child: BottomNavigationView?, rectangle: Rect?, immediate: Boolean): Boolean {
        Log.d("BMB", rectangle.toString())
        return super.onRequestChildRectangleOnScreen(coordinatorLayout, child, rectangle, immediate)
    }
}