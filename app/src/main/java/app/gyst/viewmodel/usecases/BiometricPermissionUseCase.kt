package app.gyst.viewmodel.usecases

import app.gyst.common.viewmodel.Action
import app.gyst.common.viewmodel.Result
import app.gyst.common.viewmodel.UseCase
import app.gyst.repository.AppPreferenceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOn

sealed class BiometricPermissionActions : Action {
    object Enable : BiometricPermissionActions()
    object Disable : BiometricPermissionActions()
}

data class BiometricPermissionCollectedResult(val isEnabled: Boolean) : Result

@ExperimentalCoroutinesApi
class BiometricPermissionUseCase(
    val appPreferenceRepository: AppPreferenceRepository
) : UseCase {
    override fun canProcess(action: Action): Boolean = action is BiometricPermissionActions

    override fun handleAction(action: Action): Flow<Result> = when (action) {
        BiometricPermissionActions.Enable -> handleBioMetricPermission(true)
        BiometricPermissionActions.Disable -> handleBioMetricPermission(false)
        else -> emptyFlow()
    }

    private fun handleBioMetricPermission(isEnabled: Boolean): Flow<Result> = channelFlow<Result> {
        appPreferenceRepository.enableBiometrics = isEnabled
        send(BiometricPermissionCollectedResult(isEnabled))
    }.flowOn(Dispatchers.IO)

}
