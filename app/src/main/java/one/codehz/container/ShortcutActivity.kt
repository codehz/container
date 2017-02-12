package one.codehz.container

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import one.codehz.container.ext.virtualCore

class ShortcutActivity : Activity() {
    companion object {
        val CLEAR_ALL = "one.codehz.container.CLEAR_ALL"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (intent.action) {
            CLEAR_ALL -> virtualCore.killAllApps()
        }

        finish()
    }
}