package app.gyst.ui.account.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import app.gyst.R
import app.gyst.biometrics.BiometricObserver
import app.gyst.biometrics.Biometrics
import app.gyst.biometrics.model.BioMetricResult
import app.gyst.common.exhaustive
import app.gyst.databinding.FragmentBiometricPermissionBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

@FlowPreview
@ExperimentalCoroutinesApi
class BiometricPermissionScreen : Fragment() {

    val biometrics: Biometrics by inject()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val stateObserver: Observer<BiometricPermissionScreenState> = Observer { state ->
        when (state) {
            BiometricPermissionScreenState.Initial -> renderInitialState()
            is BiometricPermissionScreenState.Navigate -> renderNavigateState(state.directions)
        }.exhaustive
    }

    val biometricObserver: BiometricObserver = BiometricObserver { bioMetricResult ->
        when (bioMetricResult) {
            BioMetricResult.Success -> biometricPermissionScreenViewModel.dispatchEvent(BiometricPermissionScreenEvents.OnEnable)
            BioMetricResult.Canceled,
            is BioMetricResult.Error -> {
            }
        }.exhaustive
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var binder: FragmentBiometricPermissionBinding

    private val biometricPermissionScreenViewModel: BiometricPermissionScreenViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binder = FragmentBiometricPermissionBinding.inflate(inflater)
        biometricPermissionScreenViewModel.state.observe(viewLifecycleOwner, stateObserver)
        return binder.root
    }

    private fun renderInitialState() {
        with(binder) {
            skip.setOnClickListener {
                biometricPermissionScreenViewModel.dispatchEvent(BiometricPermissionScreenEvents.OnSkip)
            }
            enable.setOnClickListener {
                if (biometrics.canAuthenticate()) biometrics.promptForAuth(requireContext(), biometricObserver)
            }
        }
    }

    private fun renderNavigateState(directions: NavDirections) {
        findNavController().navigate(directions, navOptions { popUpTo(R.id.navigation_main) { inclusive = true } })
    }

}