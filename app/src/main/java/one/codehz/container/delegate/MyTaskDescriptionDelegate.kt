package one.codehz.container.delegate

import android.app.ActivityManager
import com.lody.virtual.client.hook.delegate.TaskDescriptionDelegate
import com.lody.virtual.os.VUserManager
import one.codehz.container.ext.vUserManager

class MyTaskDescriptionDelegate : TaskDescriptionDelegate {
    override fun getTaskDescription(oldTaskDescription: ActivityManager.TaskDescription): ActivityManager.TaskDescription {
        val prefix = vUserManager.userName + " - "
        return when {
            oldTaskDescription.label.startsWith(prefix) -> oldTaskDescription
            else -> ActivityManager.TaskDescription(prefix + oldTaskDescription.label, oldTaskDescription.icon, oldTaskDescription.primaryColor)
        }
    }
}