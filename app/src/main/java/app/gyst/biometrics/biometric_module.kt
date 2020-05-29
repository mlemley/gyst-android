package app.gyst.biometrics

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val biometricsModule = module {
    factory {
        BiometricProvider(androidContext()).provideBiometrics()
    }
}