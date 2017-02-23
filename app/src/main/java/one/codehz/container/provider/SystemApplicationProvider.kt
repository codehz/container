package one.codehz.container.provider

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.MatrixCursor
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract.Document
import android.provider.DocumentsContract.Root
import android.provider.DocumentsProvider
import one.codehz.container.R
import one.codehz.container.ext.virtualCore
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.TimeUnit

class SystemApplicationProvider : DocumentsProvider() {
    companion object {
        private val DEFAULT_ROOT_PROJECTION = arrayOf(
                Root.COLUMN_ROOT_ID,
                Root.COLUMN_FLAGS,
                Root.COLUMN_ICON,
                Root.COLUMN_TITLE,
                Root.COLUMN_DOCUMENT_ID)

        private val DEFAULT_DOCUMENT_PROJECTION = arrayOf(
                Document.COLUMN_DOCUMENT_ID,
                Document.COLUMN_MIME_TYPE,
                Document.COLUMN_DISPLAY_NAME,
                Document.COLUMN_LAST_MODIFIED,
                Document.COLUMN_FLAGS,
                Document.COLUMN_SIZE
        )

        private fun resolveRootProjection(projection: Array<out String>?) = projection ?: DEFAULT_ROOT_PROJECTION
        private fun resolveDocumentProjection(projection: Array<out String>?) = projection ?: DEFAULT_DOCUMENT_PROJECTION
        private val DOC_ROOT_ID = "installed-apps"
    }

    override fun queryRecentDocuments(rootId: String?, projection: Array<out String>?): Cursor {
        val ret = MatrixCursor(resolveDocumentProjection(projection))
        context.packageManager.getInstalledPackages(0).filter { it.lastUpdateTime > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1) }.forEach { ret += it }
        return ret
    }

    override fun querySearchDocuments(rootId: String?, query: String, projection: Array<out String>?): Cursor {
        val ret = MatrixCursor(resolveDocumentProjection(projection))
        val internal = virtualCore.allApps.map { it.packageName }
        with(context.packageManager.getInstalledPackages(0)) {
            when (query) {
                "-s" -> filter { it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0 }
                "-3" -> filter { it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
                "-t" -> filter { it.applicationInfo.flags and ApplicationInfo.FLAG_STOPPED != 0 }
                "-g" -> filter { it.applicationInfo.flags and ApplicationInfo.FLAG_IS_GAME != 0 }
                "-c" -> filter { it.packageName in internal }
                "-f" -> filter { it.packageName !in internal }
                else -> filter { it.applicationInfo.loadLabel(context.packageManager).toString().toLowerCase().contains(query.toLowerCase()) }
            }.forEach { ret += it }
        }
        return ret
    }

    override fun openDocument(documentId: String, mode: String, signal: CancellationSignal?): ParcelFileDescriptor {
        if (ParcelFileDescriptor.parseMode(mode) != ParcelFileDescriptor.MODE_READ_ONLY)
            throw FileNotFoundException("Failed to open: $documentId, mode = $mode")
        return ParcelFileDescriptor.open(File(findApp(documentId.substring(4)).applicationInfo.sourceDir), ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun queryChildDocuments(parentDocumentId: String?, projection: Array<out String>?, sortOrder: String?): Cursor {
        val ret = MatrixCursor(resolveDocumentProjection(projection))
        context.packageManager.getInstalledPackages(0).forEach { ret += it }
        return ret
    }

    override fun queryDocument(documentId: String, projection: Array<out String>?): Cursor {
        val ret = MatrixCursor(resolveDocumentProjection(projection))
        if (documentId == "app") {
            val row = ret.newRow()
            row.add(Document.COLUMN_DOCUMENT_ID, documentId)
            row.add(Document.COLUMN_MIME_TYPE, Document.MIME_TYPE_DIR)
            row.add(Document.COLUMN_DISPLAY_NAME, context.getString(R.string.installed_application))
            row.add(Document.COLUMN_LAST_MODIFIED, System.currentTimeMillis())
//            row.add(Document.COLUMN_FLAGS, Document.FLAG_DIR_PREFERS_LAST_MODIFIED)
        } else
            ret += findApp(documentId.substring(4))
        return ret
    }

    override fun queryRoots(projection: Array<out String>?): Cursor {
        val ret = MatrixCursor(resolveRootProjection(projection))
        val row = ret.newRow()
        row.add(Root.COLUMN_ROOT_ID, DOC_ROOT_ID)
        row.add(Root.COLUMN_FLAGS, Root.FLAG_LOCAL_ONLY or Root.FLAG_SUPPORTS_RECENTS or Root.FLAG_SUPPORTS_SEARCH)
        row.add(Root.COLUMN_TITLE, context.getString(R.string.installed_application))
        row.add(Root.COLUMN_DOCUMENT_ID, "app")
        row.add(Root.COLUMN_ICON, R.drawable.ic_smartphone)
        return ret
    }

    private operator fun MatrixCursor.plusAssign(pkg: PackageInfo) {
        val file = File(pkg.applicationInfo.sourceDir)
        newRow().apply {
            add(Document.COLUMN_DOCUMENT_ID, "app:${pkg.packageName}")
            add(Document.COLUMN_MIME_TYPE, "application/vnd.android.package-archive")
            add(Document.COLUMN_DISPLAY_NAME, pkg.applicationInfo.loadLabel(context.packageManager))
            add(Document.COLUMN_LAST_MODIFIED, pkg.lastUpdateTime)
            add(Document.COLUMN_SIZE, file.length())
            add(Document.COLUMN_FLAGS, 0)
        }
    }

    private fun findApp(packageName: String)
            = context.packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA or PackageManager.GET_INSTRUMENTATION)

    override fun onCreate() = true
}