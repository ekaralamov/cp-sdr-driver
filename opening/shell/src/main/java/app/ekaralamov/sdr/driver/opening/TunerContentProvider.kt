package app.ekaralamov.sdr.driver.opening

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor

class TunerContentProvider : ContentProvider() {

    override fun insert(p0: Uri, p1: ContentValues?) = throw UnsupportedOperationException()

    override fun query(
        p0: Uri,
        p1: Array<out String>?,
        p2: String?,
        p3: Array<out String>?,
        p4: String?
    ): Cursor? {
        throw UnsupportedOperationException()
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?) =
        throw UnsupportedOperationException()

    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?) =
        throw UnsupportedOperationException()

    override fun getType(p0: Uri) = throw UnsupportedOperationException()

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        return OpeningComponent.instance.injectOpenTuner()(
            uri,
            mode,
            callingPackage ?: throw AssertionError()
        )
    }
}
