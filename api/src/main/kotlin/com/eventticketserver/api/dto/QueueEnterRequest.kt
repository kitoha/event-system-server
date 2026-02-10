package com.eventticketserver.api.dto

/**
 * 대기열 진입 요청 DTO
 */
data class QueueEnterRequest(
    val eventId: Long,
    val userId: Long
)
