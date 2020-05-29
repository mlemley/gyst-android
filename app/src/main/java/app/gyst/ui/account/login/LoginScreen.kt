package app.gyst.ui.account.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import app.gyst.R
import app.gyst.app.onSuccess
import app.gyst.app.revert
import app.gyst.app.revertWithSnackBarMessage
import app.gyst.biometrics.BiometricObserver
import app.gyst.biometrics.Biometrics
import app.gyst.biometrics.model.BioMetricResult
import app.gyst.common.*
import app.gyst.databinding.FragmentAccountLoginBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel


@FlowPreview
@ExperimentalCoroutinesApi
class LoginScreen : Fragment() {

    private val biometrics:Biometrics by inject()
    private val loginScreenViewModel: LoginScreenViewModel by viewModel()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var binder: FragmentAccountLoginBinding

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val stateObserver: Observer<LoginScreenState> = Observer { state ->
        when (state) {
            is LoginScreenState.Initial -> renderInitialState(state.withBiometrics)
            LoginScreenState.LoggingIn -> renderLoading()
            LoginScreenState.InvalidCredentials -> renderInvalidCredentials()
            LoginScreenState.ServiceFailure -> renderServiceFailure()
            is LoginScreenState.Navigate -> renderCompletedState(state.direction)
            LoginScreenState.BiometricAuthFailure -> renderBiometricAuthFailure()
        }.exhaustive
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val biometricObserver: BiometricObserver = BiometricObserver { bioMetricResult ->
        when(bioMetricResult) {
            BioMetricResult.Success -> loginScreenViewModel.dispatchEvent(LoginScreenEvents.OnLoginWithBioMetrics)
            BioMetricResult.Canceled,
            is BioMetricResult.Error -> {}
        }.exhaustive
    }

    private fun proceedToNext(direction: NavDirections) {
        findNavController().navigate(
            direction,
            navOptions { popUpTo(R.id.navigation_main) { inclusive = true } })
    }

    private fun renderCompletedState(direction: NavDirections) {
        binder.login.onSuccess().postOnAnimation { proceedToNext(direction) }
    }

    private fun renderServiceFailure() {
        binder.login.revertWithSnackBarMessage(R.string.account_login_failure_message)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binder = FragmentAccountLoginBinding.inflate(inflater)
        loginScreenViewModel.state.observe(viewLifecycleOwner, stateObserver)
        return binder.root
    }

    private fun renderInitialState(withBiometrics:Boolean) {
        with(binder) {
            login.setOnClickListener {
                performLogin()
                password.hideKeyBoard()
            }
            emailAddress.requestFocus()
            password.onImeEvent(EditorInfo.IME_ACTION_DONE) {
                performLogin()
            }

            if (withBiometrics && biometrics.canAuthenticate()) biometrics.promptForAuth(requireContext(), biometricObserver)
        }
    }

    private fun renderLoading() {
        with(binder) {
            passwordLayout.error = null
            emailAddressLayout.error = null
            login.startAnimation()
        }
    }

    private fun renderInvalidCredentials() {
        with(binder) {
            login.revert()
            emailAddressLayout.error = getString(R.string.account_login_invalid_credentials_message)
            password.setText("")
            password.requestFocus()
        }
    }

    private fun FragmentAccountLoginBinding.performLogin() {
        loginScreenViewModel.dispatchEvent(
            LoginScreenEvents.OnLogin(emailAddress.textValue, password.textValue)
        )
    }

    private fun renderBiometricAuthFailure() {
        binder.root.showSnackBar(R.string.account_login_biometric_failure_message)
    }

}
