package com.eventticketserver.api.controller

import com.eventticketserver.api.dto.TicketReserveRequest
import com.eventticketserver.api.dto.TicketReserveResponse
import com.eventticketserver.ticket.service.TicketingService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/tickets")
class TicketController(
    private val ticketingService: TicketingService
) {

    @PostMapping("/reserve")
    fun reserve(
        @RequestBody request: TicketReserveRequest
    ): TicketReserveResponse {
        val requestId = ticketingService.reserve(request.eventId, request.userId)

        return TicketReserveResponse(
            reservationId = requestId,
            status = "PENDING",
            message = "Ticket reservation is being processed"
        )
    }
}
