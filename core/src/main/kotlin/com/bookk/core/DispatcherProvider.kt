package com.bookk.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

object DispatcherProvider {
    var main: CoroutineDispatcher = Dispatchers.Main
        private set
    var default: CoroutineDispatcher = Dispatchers.Default
        private set
    var io: CoroutineDispatcher = Dispatchers.IO
        private set

    fun swapMain(dispatcher: CoroutineDispatcher) {
        main = dispatcher
    }

    fun swapDefault(dispatcher: CoroutineDispatcher) {
        default = dispatcher
    }

    fun swapIo(dispatcher: CoroutineDispatcher) {
        io = dispatcher
    }
}