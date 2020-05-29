package app.gyst.repository

import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
const val BiometricPromptKey: String = "app.gyst.BiometricPrompt.Enabled"

class AppPreferenceRepository(val sharedPreferences: SharedPreferences) {

    var enableBiometrics: Boolean
        get() = sharedPreferences.getBoolean(BiometricPromptKey, false)
        set(value) = sharedPreferences.edit { putBoolean(BiometricPromptKey, value) }

    val hasCompletedBioMetricPrompt: Boolean get() = sharedPreferences.contains(BiometricPromptKey)
}