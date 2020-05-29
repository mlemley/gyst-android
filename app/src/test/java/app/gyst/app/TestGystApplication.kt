package app.gyst.app

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.robolectric.TestLifecycleApplication
import java.lang.reflect.Method

@ExperimentalCoroutinesApi
@FlowPreview
class TestGystApplication: TestLifecycleApplication, GystApplication() {
    override fun beforeTest(method: Method?) {
        startKoin {
            androidLogger()
            androidContext(applicationContext)
        }
    }

    override fun prepareTest(test: Any?) { }

    override fun afterTest(method: Method?) {
        stopKoin()
    }


    override fun loadKoin() {
        // Leave empty
    }
}