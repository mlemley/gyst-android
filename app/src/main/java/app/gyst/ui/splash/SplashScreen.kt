package app.gyst.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import app.gyst.common.exhaustive
import app.gyst.common.navigateWithDirections
import app.gyst.databinding.FragmentSplashBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.android.viewmodel.ext.android.viewModel


@FlowPreview
@ExperimentalCoroutinesApi
class SplashScreen : Fragment() {

    private val splashScreenViewModel: SplashScreenViewModel by viewModel()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var binder: FragmentSplashBinding

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val stateObserver: Observer<SplashScreenState> = Observer {
        when (it) {
            is SplashScreenState.NavigationTask -> navigateWithDirections(it.direction)
            SplashScreenState.Initial -> {
                //do nothing
            }
        }.exhaustive
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binder = FragmentSplashBinding.inflate(inflater)
        splashScreenViewModel.state.observe(viewLifecycleOwner, stateObserver)
        return binder.root
    }

}