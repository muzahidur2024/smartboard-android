package com.smartboard.common

sealed class SmartResult<out T> {
    data class Success<T>(val data: T) : SmartResult<T>()
    data class Error(val throwable: Throwable) : SmartResult<Nothing>()

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
}
