package app.gyst.app

import android.content.Context
import app.gyst.BuildConfig
import app.gyst.biometrics.biometricsModule
import app.gyst.client.GystClientInterceptor
import app.gyst.client.SessionManager
import app.gyst.client.gystClient
import app.gyst.common.usecase.DelayUseCase
import app.gyst.persistence.persistenceModule
import app.gyst.repository.AppPreferenceRepository
import app.gyst.repository.UserAccountRepository
import app.gyst.ui.account.login.BiometricPermissionScreenViewModel
import app.gyst.ui.account.login.LoginScreenViewModel
import app.gyst.ui.financial.overview.FinancialOverviewViewModel
import app.gyst.ui.onboarding.account.create.CreateAccountViewModel
import app.gyst.ui.onboarding.account.profile.CreateProfileViewModel
import app.gyst.ui.splash.SplashScreenViewModel
import app.gyst.validation.PasswordRules
import app.gyst.viewmodel.usecases.BiometricPermissionUseCase
import app.gyst.viewmodel.usecases.account.CreateAccountUseCase
import app.gyst.viewmodel.usecases.account.CreateProfileUseCase
import app.gyst.viewmodel.usecases.account.LoginUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val SplashScreenDelay: Int = 1200

@FlowPreview
@ExperimentalCoroutinesApi
val appModule = module {

    single { SessionManager() }
    factory { PasswordRules() }
    factory { androidContext().getSharedPreferences("GystPreferences", Context.MODE_PRIVATE) }

    // View Models
    viewModel { SplashScreenViewModel(get(), SplashScreenDelay) }
    viewModel { CreateProfileViewModel(get()) }
    viewModel { CreateAccountViewModel(get(), get(), get()) }
    viewModel { LoginScreenViewModel(get(), get(), get(), get()) }
    viewModel { BiometricPermissionScreenViewModel(get()) }
    viewModel { FinancialOverviewViewModel() }

    // UseCases
    factory { DelayUseCase() }
    factory { CreateProfileUseCase(get(), get()) }
    factory { CreateAccountUseCase(get(), get(), get(), get()) }
    factory { LoginUseCase(get(), get(), get()) }
    factory { BiometricPermissionUseCase(get()) }

    // Repositories
    factory { UserAccountRepository(get(), get()) }
    factory { AppPreferenceRepository(get()) }

    // Client
    single(named("GystAPIBaseRoute")) { BuildConfig.GYST_API_BASE_ROUTE }
    factory { GystClientInterceptor(get()) }
    factory { gystClient(get(named("GystAPIBaseRoute")), get<GystClientInterceptor>()) }
}

@FlowPreview
@ExperimentalCoroutinesApi
val appModules = listOf(appModule, persistenceModule, biometricsModule)


