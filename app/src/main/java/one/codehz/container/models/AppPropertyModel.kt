package one.codehz.container.models

import one.codehz.container.R
import one.codehz.container.annotation.propertyField
import one.codehz.container.base.SameAsAble
import one.codehz.container.ext.virtualCore

class AppPropertyModel(
        val model: AppModel
) : PropertyListModel(), SameAsAble<AppPropertyModel> {
    @get:propertyField(R.string.package_name, 0)
    val packageName: String = model.packageName
    val setting = virtualCore.findApp(packageName)!!
    @get:propertyField(R.string.apk_path, 1)
    val apkPath = setting.apkPath!!
    @get:propertyField(R.string.native_library_path, 2)
    val libPath = setting.libPath!!
}