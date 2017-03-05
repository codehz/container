package one.codehz.container.ext

import android.view.View
import android.widget.TextView
import one.codehz.container.base.SameAsAble
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf

annotation class targetView<T : View>(val id: Int, val viewType: KClass<T>)

abstract class AutoAdapterModel<T : SameAsAble<T>> : SameAsAble<T>

inline fun <S: AutoAdapterModel<T>, reified T : SameAsAble<T>> KClass<S>.toBindingMap(): Map<Int, ViewBinding<T, Any>> {
    return T::class.declaredMemberProperties
            .mapNotNull { (it.findAnnotation<targetView<*>>() ?: return@mapNotNull null) to it }
            .map { it.first.id to ((findViewAssigner(it.first.viewType) to { t: T -> it.second.get(t)!! })) }
            .toMap()
}

fun <T : View> findViewAssigner(viewType: KClass<T>): (View, Any) -> Unit = when {
    viewType.isSubclassOf(TextView::class) -> { v, r ->
        (v as? TextView)?.text = r.toString()
    }
    else -> throw IllegalArgumentException()
}
