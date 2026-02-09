package com.eventticketserver.ticket.service

import com.eventticketserver.domain.event.repository.EventRepository
import com.eventticketserver.domain.ticket.entity.Ticket
import com.eventticketserver.domain.ticket.repository.TicketRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TicketingService(
    private val eventRepository: EventRepository,
    private val ticketRepository: TicketRepository,
    private val inventoryManager: InventoryManager
) {

    @Transactional
    fun reserve(eventId: Long, userId: Long): Ticket {
        val event = eventRepository.findById(eventId)
            .orElseThrow { IllegalArgumentException("Event not found with id: $eventId") }

        val success = inventoryManager.decrease(eventId)
        if (!success) {
            throw IllegalStateException("Inventory is empty")
        }

        val ticket = Ticket.reserve(event, userId)
        return ticketRepository.save(ticket)
    }
}
