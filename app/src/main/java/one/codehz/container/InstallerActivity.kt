package one.codehz.container

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class InstallerActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.action == null || intent.data == null) return finish()

        startService(Intent(this, InstallService::class.java).apply {
            this.action = InstallService.REQUEST_INSTALL
            this.data = intent.data
        })
        finish()
    }


}