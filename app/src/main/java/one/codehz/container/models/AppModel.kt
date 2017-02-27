package one.codehz.container.models

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import com.lody.virtual.remote.AppSetting
import com.lody.virtual.os.VUserHandle
import one.codehz.container.base.SameAsAble

class AppModel(val context: Context, appSetting: AppSetting) : SameAsAble<AppModel> {
    private val applicationInfo by lazy { appSetting.getApplicationInfo(VUserHandle.USER_OWNER) }
    private val packageManager by lazy { context.packageManager }
    var packageName: String = appSetting.packageName
    var name: String = applicationInfo.loadLabel(packageManager).toString()
    var icon: BitmapDrawable = try {
        applicationInfo.loadIcon(packageManager)
    } catch (e: Exception) {
        packageManager.defaultActivityIcon
    } as BitmapDrawable

    override fun equals(other: Any?) = other is AppModel && other.packageName == packageName

    override fun hashCode() = packageName.hashCode()
}
