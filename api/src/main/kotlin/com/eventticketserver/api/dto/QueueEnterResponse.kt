package com.eventticketserver.api.dto

/**
 * 대기열 진입 응답 DTO
 */
data class QueueEnterResponse(
    val token: String,
    val position: Int,
    val estimatedWaitSeconds: Int
)
