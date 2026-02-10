package com.eventticketserver.consumer.service

import com.eventticketserver.common.event.TicketReservedEvent
import com.eventticketserver.domain.event.repository.EventRepository
import com.eventticketserver.domain.ticket.entity.Ticket
import com.eventticketserver.domain.ticket.repository.TicketRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class TicketService(
    private val ticketRepository: TicketRepository,
    private val eventRepository: EventRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun saveTicketFromEvent(event: TicketReservedEvent) {
        val requestId = event.requestId.toString()

        if (ticketRepository.existsByRequestId(requestId)) {
            log.debug("Ticket already exists for requestId: {}. Skipping in REQUIRES_NEW transaction.", requestId)
            return
        }

        val eventEntity = eventRepository.findById(event.eventId)
            .orElseThrow { IllegalArgumentException("Event not found: ${event.eventId}") }

        val ticket = Ticket.reserve(
            event = eventEntity,
            userId = event.userId,
            requestId = requestId
        )

        ticketRepository.save(ticket)
        log.info("Successfully saved ticket for request: {}", requestId)
    }
}
