package app.gyst.ui.onboarding.account.profile

import androidx.navigation.NavDirections
import app.gyst.common.exhaustive
import app.gyst.common.viewmodel.*
import app.gyst.validation.InputValidationError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
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
}

sealed class CreateProfileValidationErrors : InputValidationError {
    object FirstNameEmpty : CreateProfileValidationErrors()
    object LastNameEmpty : CreateProfileValidationErrors()
}

sealed class CreateProfileActions : Action {
    data class ProcessUsersName(val firstName: String, val lastName: String) : CreateProfileActions()
}


sealed class CreateProfileResults : Result {
    data class InputInvalid(val validationErrors: List<CreateProfileValidationErrors>) :
        CreateProfileResults()

    data class InputValid(val firstName: String, val lastName: String) : CreateProfileResults()

}

@ExperimentalCoroutinesApi
class CreateProfileUseCase : UseCase {
    override fun canProcess(action: Action): Boolean = action is CreateProfileActions

    override fun handleAction(action: Action): Flow<Result> = channelFlow {
        if (action is CreateProfileActions.ProcessUsersName) {
            mutableListOf<CreateProfileValidationErrors>().apply {
                if (action.firstName.isEmpty()) add(CreateProfileValidationErrors.FirstNameEmpty)
                if (action.lastName.isEmpty()) add(CreateProfileValidationErrors.LastNameEmpty)
            }.also {
                if (it.isEmpty()) send(
                    CreateProfileResults.InputValid(
                        action.firstName,
                        action.lastName
                    )
                )
                else send(CreateProfileResults.InputInvalid(it))
            }
        }
    }
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
                    is CreateProfileResults.InputValid -> TODO()
                }.exhaustive
            }
            else -> this
        }
}
