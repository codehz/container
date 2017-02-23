package one.codehz.container.models

class RunningAppModel(val appModel: AppModel, val userId: Int) : RunningAppBaseModel(false) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as RunningAppModel

        if (appModel != other.appModel) return false
        if (userId != other.userId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = appModel.hashCode()
        result = 31 * result + userId
        return result
    }
}