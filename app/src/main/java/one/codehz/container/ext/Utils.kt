package one.codehz.container.ext

import android.app.Activity
import android.app.Dialog
import android.app.LoaderManager
import android.content.*
import android.os.AsyncTask
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v7.util.DiffUtil
import android.support.v7.util.ListUpdateCallback
import android.support.v7.widget.RecyclerView
import android.view.View
import com.lody.virtual.client.core.VirtualCore
import com.lody.virtual.client.ipc.VActivityManager
import com.lody.virtual.os.VUserManager
import one.codehz.container.base.SameAsAble
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
operator fun <T> View.get(id: Int) = this.findViewById(id) as T

@Suppress("UNCHECKED_CAST")
operator fun <T> Dialog.get(id: Int) = this.findViewById(id) as T

@Suppress("UNCHECKED_CAST")
operator fun <T> Activity.get(id: Int) = this.findViewById(id) as T

infix fun View.pair(name: String) = android.util.Pair(this, name)

val virtualCore: VirtualCore by lazy { VirtualCore.get() }
val vActivityManager: VActivityManager by lazy { VActivityManager.get() }
val vUserManager: VUserManager by lazy { VUserManager.get() }
val sharedPreferences: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(virtualCore.context) }
val clipboardManager: ClipboardManager by lazy { virtualCore.context.getSystemService(ClipboardManager::class.java)!! }

infix fun <R, P> ((P) -> R).bind(p: P) = { this.invoke(p) }

fun <T> makeAsyncTaskLoader(context: Context, task: (Context) -> T) = object : AsyncTaskLoader<T>(context) {
    override fun onStartLoading() = forceLoad()
    override fun onStopLoading() { cancelLoad() }

    override fun loadInBackground() = task(context)
}

class MakeLoaderCallbacks<T>(val contextGetter: () -> Context, val finishedFn: (T) -> Unit, val task: (Context) -> T) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = object : LoaderManager.LoaderCallbacks<T> {
        override fun onLoadFinished(loader: Loader<T>?, data: T) = finishedFn(data)
        override fun onLoaderReset(loader: Loader<T>?) = Unit
        override fun onCreateLoader(id: Int, args: Bundle?) = makeAsyncTaskLoader(contextGetter(), task)
    }
}

fun <T : SameAsAble<T>> Pair<List<T>, List<T>>.diffCallback(): DiffUtil.Callback = object : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = first[oldItemPosition] sameAs second[newItemPosition]
    override fun getOldListSize() = first.size
    override fun getNewListSize() = second.size
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = first[oldItemPosition] == second[newItemPosition]
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int) = first[oldItemPosition].getPayloads(second[newItemPosition])
}

infix fun <T : SameAsAble<T>> MutableList<T>.updateFrom(target: List<T>): (RecyclerView.Adapter<*>) -> Unit {
    val diffCb = (this to target).diffCallback()
    val diffResult = DiffUtil.calculateDiff(diffCb, true)
    return {
        this.clear()
        this.addAll(target)
        diffResult.dispatchUpdatesTo(it)
    }
}

data class AsyncTaskContext(private val publish: (() -> Unit) -> Unit) {
    fun ui(thing: () -> Unit) = publish(thing)
}

class AsyncTaskProxy<T, R>(val backgroundFn: AsyncTaskContext.(Int, T) -> R, val finishedFn: (Int, R) -> Unit) : AsyncTask<T, () -> Unit, List<R>>() {
    override fun onProgressUpdate(vararg values: () -> Unit) = values.forEach { it() }
    override fun doInBackground(vararg params: T) = params.mapIndexed { index, t -> backgroundFn.invoke(AsyncTaskContext { publishProgress(it) }, index, t) }
    override fun onPostExecute(result: List<R>) = result.forEachIndexed(finishedFn)
}

class AsyncTaskBuilder<T, R>(val context: Context, val mapFn: AsyncTaskContext.(Int, T) -> R) {

    fun then(finishedFn: (Int, R) -> Unit) = AsyncTaskProxy(mapFn, finishedFn)

    fun then(finishedFn: (R) -> Unit) = AsyncTaskProxy(mapFn) { index, input -> finishedFn(input) }

    inline fun before(preFn: () -> Unit): AsyncTaskBuilder<T, R> {
        preFn()
        return this
    }
}

fun <T, R> Context.runAsync(mapFn: AsyncTaskContext.(Int, T) -> R) = AsyncTaskBuilder<T, R>(this, mapFn)

fun <T, R> Context.runAsync(mapFn: AsyncTaskContext.(T) -> R) = AsyncTaskBuilder<T, R>(this) { index, input -> mapFn(input) }

inline fun <reified T> Context.systemService(name: String) = getSystemService(name) as T

object staticName {
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = property.name
}

fun Snackbar.setBackground(color: Int) = apply { view.setBackgroundColor(color) }

fun <T> Iterable<T>.onEach(thing: (T) -> Unit) = map { thing(it); it }
fun <T> Sequence<T>.onEach(thing: (T) -> Unit) = map { thing(it); it }

inline fun <reified T : Parcelable> createParcel(crossinline createFromParcel: (Parcel) -> T?): Parcelable.Creator<T> =
        object : Parcelable.Creator<T> {
            override fun createFromParcel(source: Parcel): T? = createFromParcel(source)
            override fun newArray(size: Int): Array<out T?> = arrayOfNulls(size)
        }
