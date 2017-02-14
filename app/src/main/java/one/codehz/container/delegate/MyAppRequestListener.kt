package one.codehz.container.delegate

import android.content.Intent
import android.net.Uri
import com.lody.virtual.client.core.VirtualCore
import one.codehz.container.InstallService
import one.codehz.container.ext.virtualCore
import java.io.File

class MyAppRequestListener : VirtualCore.AppRequestListener {
    override fun onRequestInstall(path: String) {
        virtualCore.context.startService(Intent(virtualCore.context, InstallService::class.java).apply {
            this.action = InstallService.REQUEST_INSTALL
            this.data = Uri.fromFile(File(path))
        })
    }

    override fun onRequestUninstall(pkg: String) {
        virtualCore.context.startService(Intent(virtualCore.context, InstallService::class.java).apply {
            this.action = InstallService.REQUEST_UNINSTALL
            this.putExtra(InstallService.KEY_PACKAGE_NAME, pkg)
        })
    }
}