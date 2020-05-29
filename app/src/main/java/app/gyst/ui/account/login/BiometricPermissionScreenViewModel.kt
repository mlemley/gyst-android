package app.gyst.ui.account.login

import androidx.navigation.NavDirections
import app.gyst.R
import app.gyst.common.asNavDirection
import app.gyst.common.exhaustive
import app.gyst.common.viewmodel.*
import app.gyst.viewmodel.usecases.BiometricPermissionActions
import app.gyst.viewmodel.usecases.BiometricPermissionCollectedResult
import app.gyst.viewmodel.usecases.BiometricPermissionUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

sealed class BiometricPermissionScreenEvents : Event {
    object OnSkip : BiometricPermissionScreenEvents()
    object OnEnable : BiometricPermissionScreenEvents()
}

sealed class BiometricPermissionScreenState : State {
    object Initial : BiometricPermissionScreenState()
    data class Navigate(val directions: NavDirections): BiometricPermissionScreenState()
}

@FlowPreview
@ExperimentalCoroutinesApi
class BiometricPermissionScreenViewModel(
    biometricPermissionUseCase: BiometricPermissionUseCase
) : BaseViewModel<BiometricPermissionScreenEvents, BiometricPermissionScreenState>() {
    override val useCases: List<UseCase> = listOf(biometricPermissionUseCase)

    override fun makeInitState(): BiometricPermissionScreenState = BiometricPermissionScreenState.Initial

    override fun Flow<BiometricPermissionScreenEvents>.eventTransform(): Flow<Action> = flow {
        collect { event ->
            when (event) {
                BiometricPermissionScreenEvents.OnSkip -> emit(BiometricPermissionActions.Disable)
                BiometricPermissionScreenEvents.OnEnable -> emit(BiometricPermissionActions.Enable)
            }.exhaustive
        }
    }

    override fun BiometricPermissionScreenState.plus(result: Result): BiometricPermissionScreenState = when (result) {
        is BiometricPermissionCollectedResult -> BiometricPermissionScreenState.Navigate(R.id.nav_introduction_screen.asNavDirection())
        else -> this
    }

}