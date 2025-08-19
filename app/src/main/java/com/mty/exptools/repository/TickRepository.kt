package com.mty.exptools.repository

import com.mty.exptools.logic.dao.PreferenceDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.isActive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TickRepository @Inject constructor() {

    /** 自动刷新间隔：默认 10 秒 */
    private val autoRefreshSeconds: Flow<Int> = PreferenceDao.observeAutoRefreshSeconds()
        .distinctUntilChanged()

    /** 根据设置动态切换的“心跳流”：每 tick 发一个 Unit */
    @OptIn(ExperimentalCoroutinesApi::class)
    val autoRefreshTicker: Flow<Unit> =
        autoRefreshSeconds
            .flatMapLatest { sec ->
                if (sec <= 0) emptyFlow()
                else tickerFlow(periodMs = sec * 1000L)
            }
            .shareIn(
                scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
                started = SharingStarted.Lazily,
                replay = 0
            )

    private fun tickerFlow(periodMs: Long) = flow {
        while (currentCoroutineContext().isActive) {
            emit(Unit)
            delay(periodMs)
        }
    }

}