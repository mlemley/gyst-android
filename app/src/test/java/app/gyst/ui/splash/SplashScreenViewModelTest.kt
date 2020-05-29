package app.gyst.ui.splash

import app.gyst.common.usecase.DelayComplete
import app.gyst.common.usecase.DelayFor
import app.gyst.common.usecase.DelayUseCase
import app.gyst.common.viewmodel.Action
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking

import org.junit.Test
import java.time.Duration

@FlowPreview
@ExperimentalCoroutinesApi
class SplashScreenViewModelTest {

    companion object {
        const val DelayDuration: Int = 1_000
    }

    private fun createViewModel(
        delayUseCase: DelayUseCase = mockk(relaxUnitFun = true)
    ): SplashScreenViewModel = SplashScreenViewModel(delayUseCase, DelayDuration)

    @Test
    fun make_initial_state__dispatches_timer() {
        val viewModel = createViewModel()
        assertThat(viewModel.makeInitState()).isEqualTo(SplashScreenState.Initial)
    }

    @Test
    fun provides_list_of_use_cases() {
        val delayUseCase: DelayUseCase = mockk(relaxUnitFun = true)
        assertThat(createViewModel(delayUseCase).useCases).isEqualTo(listOf(delayUseCase))
    }

    @Test
    fun transforms__view_model__events__to__use_case__actions() {
        val events = flowOf(
            SplashEvents.Load(DelayDuration)
        )

        val expectedActions = listOf(DelayFor(DelayDuration))

        val actualActions = mutableListOf<Action>()

        with(createViewModel()) {
            runBlocking {
                events.eventTransform().toList(actualActions)
            }
        }

        assertThat(actualActions).isEqualTo(expectedActions)
    }

    @Test
    fun updates_state___when_delay_completed() {
        val viewModel = createViewModel()
        val initialState = viewModel.makeInitState()

        val results = listOf(DelayComplete)

        val actualStates = mutableListOf<SplashScreenState>()
        with(viewModel) {
            results.forEach { actualStates.add(initialState + it) }
        }
    }
}