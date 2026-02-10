package com.eventticketserver.queue.service

/**
 * 대기열 위치 정보
 */
data class QueuePosition(
    val position: Int,
    val estimatedWaitSeconds: Int
)
