package one.codehz.container.annotation

@Target(AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class propertyField(val nameRes: Int, val order: Int = 0)
