package app.gyst.common

import android.content.Context
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZoneOffset.UTC
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter


fun String.isValidEmail() = Patterns.EMAIL_ADDRESS.matcher(this).matches()

val <T> T.exhaustive: T get() = this

fun View.show() = apply { visibility = View.VISIBLE }

fun View.hide() = apply { visibility = View.INVISIBLE }

fun View.gone() = apply { visibility = View.GONE }

fun View.hideKeyBoard() = apply {
    with(context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager) {
        hideSoftInputFromWindow(this@apply.windowToken, 0);
    }
}

sealed class SnackBarLength(val len: Int) {
    object Long : SnackBarLength(Snackbar.LENGTH_LONG)
    object Short : SnackBarLength(Snackbar.LENGTH_SHORT)
    object Indefinite : SnackBarLength(Snackbar.LENGTH_INDEFINITE)

    fun toInt(): Int = len
}

fun View.showSnackBar(@StringRes message: Int, length: SnackBarLength = SnackBarLength.Long) =
    Snackbar.make(this, message, length.toInt()).show()

fun EditText.onImeEvent(whichEvent: Int, block: () -> Unit) {
    this.setOnEditorActionListener { v, actionId, event ->
        return@setOnEditorActionListener when (actionId) {
            whichEvent -> {
                block()
                true
            }
            else -> false
        }
    }
}

val ByteArray.asString: String get() = String(this)

val Buffer.asByteArray
    get() = ByteArray(this.size.toInt()).also {
        this.read(it)
    }

val RequestBody.asBuffer: Buffer
    get() = Buffer().also {
        this.writeTo(it)
    }

val RequestBody.asByteArray: ByteArray get() = this.asBuffer.asByteArray

val RequestBody.asString: String get() = this.asByteArray.asString

val ResponseBody.asString: String get() = this.asByteArray.asString
val ResponseBody.asByteArray: ByteArray get() = this.bytes()

fun Response.log() {
    this.body?.let { Log.d(it.javaClass.simpleName, it.asString) }
}

fun Request.log() {
    this.body?.let { Log.d(it.javaClass.simpleName, it.asString) }
}

fun Any.log() = Log.d(this.javaClass.simpleName, this.toString())

val TextView.textValue: String get() = this.text.toString()
val EditText.textValue: String get() = this.text.toString()


fun Long.toInstant(): Instant = Instant.ofEpochMilli(this)
fun Instant.utc(): ZonedDateTime = this.atZone(UTC)
fun Instant.local(): ZonedDateTime = this.atZone(ZoneOffset.systemDefault())
const val Iso8601Pattern: String = "yyyy-MM-dd'T'HH:mm:ss'Z'"
fun ZonedDateTime.toIso8601TimeStamp(pattern: String = Iso8601Pattern): String {
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return formatter.format(this)
}

fun String.toInstant(): Instant? {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':'mm[':'ss[.SSSSSSSSS][.SSSSSS][.SSS]]'Z'")
    return LocalDateTime.parse(this, formatter).toInstant(UTC)
}

fun Int.asNavDirection(): NavDirections = ActionOnlyNavDirections( this)

fun Fragment.navigateWithDirections(navDirections: NavDirections, navOptions: NavOptions? = null) {
    with(findNavController()) {
        navigate(
            navDirections,
            navOptions
        )
    }
}
