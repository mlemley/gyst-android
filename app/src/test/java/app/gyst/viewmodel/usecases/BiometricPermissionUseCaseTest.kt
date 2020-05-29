package app.gyst.viewmodel.usecases

import app.gyst.common.viewmodel.Action
import app.gyst.common.viewmodel.Result
import app.gyst.repository.AppPreferenceRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test

@ExperimentalCoroutinesApi
class BiometricPermissionUseCaseTest {

    private fun createUseCase(
        appPreferenceRepository: AppPreferenceRepository = mockk(relaxed = true)
    ): BiometricPermissionUseCase = BiometricPermissionUseCase(appPreferenceRepository)

    @Test
    fun handles_actions() {
        val useCase = createUseCase()
        assertThat(useCase.canProcess(BiometricPermissionActions.Disable)).isTrue()
        assertThat(useCase.canProcess(BiometricPermissionActions.Enable)).isTrue()
        assertThat(useCase.canProcess(object : Action {})).isFalse()
    }

    @Test
    fun handles_action__disable_permission() = runBlocking {
        val appPreferenceRepository: AppPreferenceRepository = mockk(relaxed = true)
        val useCase = createUseCase(appPreferenceRepository)

        val results = mutableListOf<Result>()
        useCase.handleAction(BiometricPermissionActions.Disable).toList(results)

        assertThat(results).isEqualTo(listOf(BiometricPermissionCollectedResult(false)))
        verify { appPreferenceRepository.enableBiometrics = false }
    }

    @Test
    fun handles_action__enable_permission() = runBlocking {
        val appPreferenceRepository: AppPreferenceRepository = mockk(relaxed = true)
        val useCase = createUseCase(appPreferenceRepository)

        val results = mutableListOf<Result>()
        useCase.handleAction(BiometricPermissionActions.Enable).toList(results)

        assertThat(results).isEqualTo(listOf(BiometricPermissionCollectedResult(true)))
        verify { appPreferenceRepository.enableBiometrics = true }
    }
}