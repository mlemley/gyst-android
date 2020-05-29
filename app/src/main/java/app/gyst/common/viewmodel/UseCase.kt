package app.gyst.common.viewmodel

import kotlinx.coroutines.flow.Flow

interface UseCase {
    fun canProcess(action: Action): Boolean
    fun handleAction(action: Action): Flow<Result>
}