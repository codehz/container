package one.codehz.container.annotation

@Target(AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class propertyField(val name: String = "", val order: Int = 0)
