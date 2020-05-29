package app.gyst.biometrics.ktx

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricManager
import android.os.Build
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import javax.crypto.Cipher


@RequiresApi(Build.VERSION_CODES.Q)
fun Context.bioMetricsManager(): BiometricManager =
    this.getSystemService(BiometricManager::class.java) as BiometricManager

fun Context.fingerprintManger(): FingerprintManagerCompat = FingerprintManagerCompat.from(this)

fun Context.hasSelfPermission(permission: String): Boolean =
    this.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED


fun Cipher.asCryptoObject(): FingerprintManagerCompat.CryptoObject = FingerprintManagerCompat.CryptoObject(this)

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.hide() {
    this.visibility = View.INVISIBLE
}

fun TextView.goneIfEmpty() {
    if (this.text.isEmpty()) this.gone()
}