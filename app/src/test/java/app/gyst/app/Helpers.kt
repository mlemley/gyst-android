package app.gyst.app

import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module

@FlowPreview
@ExperimentalCoroutinesApi
fun loadModules(vararg modules: Module) {
    ApplicationProvider.getApplicationContext<TestGystApplication>().also {
        loadKoinModules(
            modules.toList()
        )
    }
}

fun Fragment.withMockedNavigation():NavController {
    val navController: NavController = mockk(relaxed = true)
    viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
        if (viewLifecycleOwner != null) {
            // The fragment’s view has just been created
            Navigation.setViewNavController(requireView(), navController)
        }
    }
    return navController
}

fun NavController.verifyNavigation(verificationClosure: (directions: NavDirections, options: NavOptions) -> Unit) {
    val directionSlot = slot<NavDirections>()
    val optionsSlot = slot<NavOptions>()
    verify {
        navigate(capture(directionSlot), capture(optionsSlot))
    }
    verificationClosure(directionSlot.captured, optionsSlot.captured)
}
