package com.eventticketserver.api.dto

/**
 * 티켓 예약 응답 DTO
 */
data class TicketReserveResponse(
    val reservationId: String,
    val status: String,
    val message: String
)
