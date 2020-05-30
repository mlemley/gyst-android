package app.gyst.ui.onboarding.account.profile

import androidx.navigation.NavDirections
import app.gyst.common.exhaustive
import app.gyst.common.viewmodel.*
import app.gyst.validation.InputValidationError
import app.gyst.viewmodel.usecases.account.CreateProfileActions
import app.gyst.viewmodel.usecases.account.CreateProfileResults
import app.gyst.viewmodel.usecases.account.CreateProfileUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

sealed class CreateProfileEvents : Event {
    data class OnProcessNext(val firstName: String, val lastName: String) :
        CreateProfileEvents()
}

sealed class CreateProfileState : State {
    object Initial : CreateProfileState()
    data class InvalidInput(val validationErrors: List<CreateProfileValidationErrors>) :
        CreateProfileState()

    data class NavigationTask(val direction: NavDirections) : CreateProfileState()
    object CreateProfileFailure : CreateProfileState()
}

sealed class CreateProfileValidationErrors : InputValidationError {
    object FirstNameEmpty : CreateProfileValidationErrors()
    object LastNameEmpty : CreateProfileValidationErrors()
}


@FlowPreview
@ExperimentalCoroutinesApi
class CreateProfileViewModel(
    createProfileUseCase: CreateProfileUseCase
) : BaseViewModel<CreateProfileEvents, CreateProfileState>() {
    override val useCases: List<UseCase> = listOf(createProfileUseCase)

    override fun makeInitState(): CreateProfileState =
        CreateProfileState.Initial

    override fun Flow<CreateProfileEvents>.eventTransform(): Flow<Action> = flow {
        collect { event ->
            when (event) {
                is CreateProfileEvents.OnProcessNext -> emit(
                    CreateProfileActions.ProcessUsersName(
                        event.firstName,
                        event.lastName
                    )
                )
            }.exhaustive
        }
    }

    override fun CreateProfileState.plus(result: Result): CreateProfileState =
        when (result) {
            is CreateProfileResults -> {
                when (result) {
                    is CreateProfileResults.InputInvalid -> CreateProfileState.InvalidInput(
                        result.validationErrors
                    )
                    CreateProfileResults.ProfileCreated -> CreateProfileState.NavigationTask(CreateProfileScreenDirections.actionNavIntroductionScreenToNavFinancialOverview())
                    CreateProfileResults.CreateProfileFailed -> CreateProfileState.CreateProfileFailure
                }.exhaustive
            }
            else -> this
        }
}
