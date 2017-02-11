package one.codehz.container.models

import one.codehz.container.annotation.propertyField
import one.codehz.container.base.SameAsAble
import one.codehz.container.ext.virtualCore

class AppPropertyModel(
        val model: AppModel
) : PropertyListModel(), SameAsAble<AppPropertyModel> {
    @get:propertyField("Package Name", 0)
    val packageName: String = model.packageName
    val setting = virtualCore.findApp(packageName)!!
    @get:propertyField("Apk Path", 1)
    val apkPath = setting.apkPath!!
    @get:propertyField("Native Library Path", 2)
    val libPath = setting.libPath!!
}