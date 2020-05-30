package app.gyst.ui.financial.overview

import app.gyst.common.exhaustive
import app.gyst.common.viewmodel.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

sealed class FinancialOverviewViewState : State {
    object Initial : FinancialOverviewViewState()
}

sealed class FinancialOverviewEvents : Event {

}


@FlowPreview
@ExperimentalCoroutinesApi
class FinancialOverviewViewModel : BaseViewModel<FinancialOverviewEvents, FinancialOverviewViewState>() {
    override val useCases: List<UseCase> = emptyList()

    override fun makeInitState(): FinancialOverviewViewState = FinancialOverviewViewState.Initial

    override fun Flow<FinancialOverviewEvents>.eventTransform(): Flow<Action> = flow {
        collect { event ->
            when (event) {
                else -> TODO()
            }.exhaustive
        }
    }

    override fun FinancialOverviewViewState.plus(result: Result): FinancialOverviewViewState = when (result) {
        else -> this
    }

}
