package app.gyst.ui.onboarding.account.profile

import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.LiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.gyst.R
import app.gyst.app.loadModules
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

@FlowPreview
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class CreateProfileScreenTest {

    private fun createScenario(
        liveData: LiveData<CreateProfileState> = mockk(relaxUnitFun = true),
        createProfileViewModel: CreateProfileViewModel = mockk(relaxUnitFun = true) {
            every { state } returns liveData
        }
    ): FragmentScenario<CreateProfileScreen> {
        loadModules(
            module {
                viewModel { createProfileViewModel }
            }
        )

        return FragmentScenario.launchInContainer(
            CreateProfileScreen::class.java,
            null,
            R.style.GystTheme,
            null
        ).onFragment { fragment ->
            fragment.stateObserver.onChanged(CreateProfileState.Initial)
        }
    }

    // View Setup

    @Test
    fun observes_state_changes() {
        val state: LiveData<CreateProfileState> = mockk(relaxUnitFun = true)

        createScenario(state).onFragment { fragment ->
            verify { state.observe(fragment.viewLifecycleOwner, fragment.stateObserver) }
        }
    }

    // State Renderng
}