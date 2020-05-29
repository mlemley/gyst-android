package app.gyst.repository

import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit


class OnboardingRepository(
    val sharedPreferences: SharedPreferences
) {
    fun onBoardingComplete() = sharedPreferences.edit {
        putBoolean(PreferenceKey, true)
    }

    val hasOnBoarded: Boolean get() = sharedPreferences.getBoolean(PreferenceKey, false)


    companion object {
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        const val PreferenceKey:String = "app.gyst.repository.OnboardingRepository::HasOnBoarded"
    }
}