package app.gyst.app

import androidx.test.core.app.ApplicationProvider
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
