package com.eventticketserver.api.dto

/**
 * 티켓 예약 요청 DTO
 */
data class TicketReserveRequest(
    val eventId: Long,
    val userId: Long,
    val quantity: Int = 1
)
