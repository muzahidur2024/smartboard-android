package com.smartboard.common

import kotlinx.coroutines.CoroutineDispatcher

interface SmartBoardDispatchers {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}
