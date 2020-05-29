package app.gyst.ui.splash

import androidx.navigation.NavDirections
import app.gyst.common.exhaustive
import app.gyst.common.usecase.DelayComplete
import app.gyst.common.usecase.DelayFor
import app.gyst.common.usecase.DelayUseCase
import app.gyst.common.viewmodel.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow


sealed class SplashScreenState : State {
    object Initial : SplashScreenState()
    data class NavigationTask(val direction: NavDirections) : SplashScreenState()
}

sealed class SplashEvents : Event {
    data class Load(val duration: Int) : SplashEvents()
}

@FlowPreview
@ExperimentalCoroutinesApi
class SplashScreenViewModel(
    delayUseCase: DelayUseCase,
    private val delayDuration: Int
) : BaseViewModel<SplashEvents, SplashScreenState>() {

    override val useCases: List<UseCase> = listOf(delayUseCase)

    override fun makeInitState(): SplashScreenState = SplashScreenState.Initial.also {
        dispatchEvent(SplashEvents.Load(delayDuration))
    }

    override fun Flow<SplashEvents>.eventTransform(): Flow<Action> = flow {
        collect { event ->
            when (event) {
                is SplashEvents.Load -> emit(DelayFor(event.duration))
            }.exhaustive
        }
    }

    override fun SplashScreenState.plus(result: Result): SplashScreenState = when (result){
        DelayComplete -> SplashScreenState.NavigationTask(SplashScreenDirections.actionNavSplashScreenToNavOnboardingCreateAccount())
        else -> this
    }

}
