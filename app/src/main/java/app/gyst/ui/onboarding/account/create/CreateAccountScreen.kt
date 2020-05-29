package app.gyst.ui.onboarding.account.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.annotation.VisibleForTesting
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import app.gyst.R
import app.gyst.app.revertWithSnackBarMessage
import app.gyst.common.exhaustive
import app.gyst.common.hideKeyBoard
import app.gyst.common.onImeEvent
import app.gyst.databinding.FragmentOnboardingCreateAccountBinding
import app.gyst.validation.PasswordRules
import app.gyst.validation.PasswordStrength
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.android.viewmodel.ext.android.viewModel

@FlowPreview
@ExperimentalCoroutinesApi
class CreateAccountScreen : Fragment() {

    private val createAccountViewModel: CreateAccountViewModel by viewModel()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var binder: FragmentOnboardingCreateAccountBinding

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val stateObserver: Observer<CreateAccountState> = Observer { state ->
        when (state) {
            CreateAccountState.Loading -> renderLoadingState()
            is CreateAccountState.Initial -> renderInitialState(passwordRules = state.passwordRules)
            CreateAccountState.InvalidEmail -> renderInvalidEmailState()
            is CreateAccountState.PasswordEntry -> renderPasswordEntryState(state.missing)
            is CreateAccountState.Navigate -> handleNavigateCommand(state.direction)
            CreateAccountState.AccountExists -> renderAccountExistsState()
            CreateAccountState.AccountCreationFailure -> renderAccountCreationFailureState()
        }.exhaustive
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binder = FragmentOnboardingCreateAccountBinding.inflate(layoutInflater)
        createAccountViewModel.state.observe(viewLifecycleOwner, stateObserver)
        return binder.root
    }

    private fun FragmentOnboardingCreateAccountBinding.createAccount() {
        password.hideKeyBoard()
        createAccountViewModel.dispatchEvent(
            CreateAccountEvents.OnCreateAccount(
                emailAddress.text.toString(),
                password.text.toString()
            )
        )
    }

    private fun renderInitialState(passwordRules: PasswordRules) {
        with(binder) {
            login.setOnClickListener {
                findNavController().navigate(R.id.nav_login_screen)
            }

            rulesDigits.isEnabled = false
            rulesLower.isEnabled = false
            rulesUpper.isEnabled = false
            rulesSpecial.apply {
                isEnabled = false
                text = getString(R.string.password_rules_special, passwordRules.specialChars)
            }
            rulesLength.apply {
                isEnabled = false
                text = getString(R.string.password_rules_size, passwordRules.minSize.toString(), passwordRules.maxSize.toString())
            }
            password.setText("")
            password.doAfterTextChanged {
                it?.let {
                    createAccountViewModel.dispatchEvent(
                        CreateAccountEvents.OnPasswordStream(
                            it.toString()
                        )
                    )
                }
            }

            password.onImeEvent(EditorInfo.IME_ACTION_DONE) {
                createAccount()
            }

            next.setOnClickListener {
                createAccount()
            }
        }
    }


    private fun renderLoadingState() {
        with(binder) {
            next.startAnimation()
            passwordLayout.error = null
            emailAddressLayout.error = null
        }
    }

    private fun renderInvalidEmailState() {
        with(binder) {
            next.revertAnimation()
            password.setText("")
            passwordLayout.error = null
            emailAddressLayout.error = getString(R.string.invalid_email)
        }
    }

    private fun renderPasswordEntryState(missing: List<PasswordStrength>) {
        with(binder) {
            next.revertAnimation()
            rulesLower.isEnabled = false
            rulesUpper.isEnabled = false
            rulesDigits.isEnabled = false
            rulesSpecial.isEnabled = false
            rulesLength.isEnabled = false

            (PasswordStrength.all() - missing).forEach {
                when (it) {
                    PasswordStrength.LowerCased -> rulesLower.isEnabled = true
                    PasswordStrength.UpperCased -> rulesUpper.isEnabled = true
                    PasswordStrength.Digits -> rulesDigits.isEnabled = true
                    PasswordStrength.SpecialValues -> rulesSpecial.isEnabled = true
                    PasswordStrength.Length -> rulesLength.isEnabled = true
                }.exhaustive
            }
        }
    }

    private fun handleNavigateCommand(directions: NavDirections) {
        findNavController().navigate(directions, navOptions { popUpTo(R.id.navigation_main) { inclusive = true } })
    }

    private fun renderAccountCreationFailureState() {
        binder.next.revertWithSnackBarMessage(R.string.account_creation_failure_message)
    }

    private fun renderAccountExistsState() {
        binder.next.revertWithSnackBarMessage(R.string.account_creation_conflict_failure_message)
    }
}