package app.gyst.ui.onboarding.account.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import app.gyst.R
import app.gyst.common.exhaustive
import app.gyst.databinding.FragmentOnboardingCreateProfileBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.android.viewmodel.ext.android.viewModel

@FlowPreview
@ExperimentalCoroutinesApi
class CreateProfileScreen : Fragment() {

    private val createProfileViewModel: CreateProfileViewModel by viewModel()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var binder: FragmentOnboardingCreateProfileBinding

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val stateObserver: Observer<CreateProfileState> = Observer { state ->
        when (state) {
            CreateProfileState.Initial -> renderInitialState()
            is CreateProfileState.InvalidInput -> renderInvalidInput(state.validationErrors)
            is CreateProfileState.NavigationTask -> findNavController().navigate(state.direction)
            CreateProfileState.CreateProfileFailure -> renderProfileFailure()
        }.exhaustive
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binder = FragmentOnboardingCreateProfileBinding.inflate(inflater)
        createProfileViewModel.state.observe(viewLifecycleOwner, stateObserver)
        return binder.root
    }

    private fun renderInitialState() {

        binder.next.setOnClickListener {
            binder.firstNameLayout.error = null
            binder.lastNameLayout.error = null
            createProfileViewModel.dispatchEvent(
                CreateProfileEvents.OnProcessNext(
                    binder.firstName.text.toString(),
                    binder.lastName.text.toString()
                )
            )
        }
    }

    private fun renderInvalidInput(validationErrors: List<CreateProfileValidationErrors>) {

        validationErrors.forEach {
            when (it) {
                CreateProfileValidationErrors.FirstNameEmpty -> {
                    binder.firstNameLayout.error = getString(
                        R.string.first_name_empty_error
                    )
                }
                CreateProfileValidationErrors.LastNameEmpty -> {
                    binder.lastNameLayout.error = getString(
                        R.string.last_name_empty_error
                    )
                }
            }.exhaustive
        }

    }

    private fun renderProfileFailure() {
        TODO("Not yet implemented")
    }
}

