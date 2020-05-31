package app.gyst.ui.onboarding.account.profile

import app.gyst.common.viewmodel.Action
import app.gyst.common.viewmodel.Result
import app.gyst.viewmodel.usecases.account.CreateProfileActions
import app.gyst.viewmodel.usecases.account.CreateProfileResults
import app.gyst.viewmodel.usecases.account.CreateProfileUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test

@FlowPreview
@ExperimentalCoroutinesApi
class CreateProfileViewModelTest {

    private fun createViewModel(
        createProfileUseCase: CreateProfileUseCase = mockk(relaxUnitFun = true)
    ): CreateProfileViewModel = CreateProfileViewModel(createProfileUseCase)

    @Test
    fun provides_list_of_its_used_use_cases() {
        val createProfileUseCase: CreateProfileUseCase = mockk(relaxUnitFun = true)
        assertThat(createViewModel(createProfileUseCase).useCases).isEqualTo(listOf(createProfileUseCase))
    }

    @Test
    fun creates_initial_state() {
        assertThat(createViewModel().makeInitState()).isEqualTo(CreateProfileState.Initial)
    }

    @Test
    fun maps__view_model_events__to__use_case_actions() = runBlocking {
        val firstName = "--first-name--"
        val lastName = "--last-name--"
        val events = flowOf(CreateProfileEvents.OnProcessNext(firstName, lastName))

        val actualActions = mutableListOf<Action>()
        with(createViewModel()) {
            events.eventTransform().toList(actualActions)
        }

        assertThat(actualActions).isEqualTo(
            listOf(
                CreateProfileActions.ProcessUsersName(firstName, lastName)
            )
        )
    }

    @Test
    fun maps__results__to__states() {
        val viewModel = createViewModel()
        val initialState = viewModel.makeInitState()
        val actualStates = mutableListOf<CreateProfileState>()
        val results = listOf<Result>(
            CreateProfileResults.InputInvalid(listOf(CreateProfileValidationErrors.FirstNameEmpty)),
            CreateProfileResults.ProfileCreated,
            CreateProfileResults.CreateProfileFailed
        )

        with(viewModel) {
            results.forEach {
                actualStates.add(initialState + it)
            }
        }

        assertThat(actualStates).isEqualTo(
            listOf(
                CreateProfileState.InvalidInput(listOf(CreateProfileValidationErrors.FirstNameEmpty)),
                CreateProfileState.NavigationTask(
                    CreateProfileScreenDirections.actionNavIntroductionScreenToNavFinancialOverview()
                ),
                CreateProfileState.CreateProfileFailure
            )
        )
    }
}