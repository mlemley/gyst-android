package app.gyst.common.usecase

import app.gyst.common.viewmodel.Action
import app.gyst.common.viewmodel.Result
import app.gyst.common.viewmodel.UseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn

data class DelayFor(val duration:Int): Action
object DelayComplete: Result

@ExperimentalCoroutinesApi
class DelayUseCase: UseCase {

    override fun canProcess(action: Action): Boolean = action is DelayFor

    override fun handleAction(action: Action): Flow<Result> = channelFlow<Result> {
        if (action is DelayFor) {
            delay(action.duration.toLong())
            send(DelayComplete)
        }
    }.flowOn(Dispatchers.IO)

}
