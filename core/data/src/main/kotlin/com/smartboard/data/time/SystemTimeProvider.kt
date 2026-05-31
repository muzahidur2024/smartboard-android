package com.smartboard.data.time

import com.smartboard.common.TimeProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemTimeProvider @Inject constructor() : TimeProvider {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}
