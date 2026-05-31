package com.smartboard.common

fun interface TimeProvider {
    fun currentTimeMillis(): Long
}
