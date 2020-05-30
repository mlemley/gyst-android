package app.gyst.viewmodel.usecases.account

import app.gyst.client.GystClient
import app.gyst.client.SafeResponse
import app.gyst.client.model.UserProfileUpdateRequest
import app.gyst.client.safeCall
import app.gyst.common.viewmodel.Action
import app.gyst.common.viewmodel.Result
import app.gyst.common.viewmodel.UseCase
import app.gyst.repository.UserAccountRepository
import app.gyst.ui.onboarding.account.profile.CreateProfileValidationErrors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking

sealed class CreateProfileActions : Action {
    data class ProcessUsersName(val firstName: String, val lastName: String) : CreateProfileActions()
}


sealed class CreateProfileResults : Result {
    data class InputInvalid(val validationErrors: List<CreateProfileValidationErrors>) :
        CreateProfileResults()

    object ProfileCreated : CreateProfileResults()
    object CreateProfileFailed : CreateProfileResults()
}

@ExperimentalCoroutinesApi
class CreateProfileUseCase(
    val gystClient: GystClient,
    val userAccountRepository: UserAccountRepository
) : UseCase {
    override fun canProcess(action: Action): Boolean = action is CreateProfileActions

    override fun handleAction(action: Action): Flow<Result> = channelFlow {
        if (action is CreateProfileActions.ProcessUsersName) {
            mutableListOf<CreateProfileValidationErrors>().apply {
                if (action.firstName.isEmpty()) add(CreateProfileValidationErrors.FirstNameEmpty)
                if (action.lastName.isEmpty()) add(CreateProfileValidationErrors.LastNameEmpty)
            }.also { errors ->
                if (errors.isNotEmpty()) {
                    send(CreateProfileResults.InputInvalid(errors))
                    return@channelFlow
                }
            }

            safeCall { runBlocking { gystClient.createUserProfile(UserProfileUpdateRequest(action.firstName, action.lastName)) } }.let {
                when (it) {
                    is SafeResponse.Success -> {
                        it.data?.let {
                            userAccountRepository.saveUserProfile(it)
                            send(CreateProfileResults.ProfileCreated)
                        } ?: send(CreateProfileResults.CreateProfileFailed)
                    }
                    is SafeResponse.Error -> send(CreateProfileResults.CreateProfileFailed)
                }
            }
        }
    }.flowOn(Dispatchers.IO)
}
