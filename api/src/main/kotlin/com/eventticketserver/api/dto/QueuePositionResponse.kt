package com.eventticketserver.api.dto

/**
 * 실시간 대기 순번 응답 DTO (SSE용)
 */
data class QueuePositionResponse(
    val position: Int,
    val estimatedWaitSeconds: Int
)
