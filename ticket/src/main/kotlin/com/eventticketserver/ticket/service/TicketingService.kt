package com.eventticketserver.ticket.service

import com.eventticketserver.common.event.TicketReservedEvent
import com.eventticketserver.domain.event.repository.EventRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class TicketingService(
    private val eventRepository: EventRepository,
    private val inventoryManager: InventoryManager,
    private val ticketEventProducer: TicketEventProducer
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun reserve(eventId: Long, userId: Long): String {
        val event = eventRepository.findById(eventId)
            .orElseThrow { IllegalArgumentException("Event not found with id: $eventId") }

        check(event.isOpen()) { "Ticket reservation is not available yet" }

        val requestId = UUID.randomUUID().toString()
        val eventPayload = TicketReservedEvent.newBuilder()
            .setEventId(eventId)
            .setUserId(userId)
            .setRequestId(requestId)
            .setCreatedAt(LocalDateTime.now().toString())
            .build()

        val success = inventoryManager.decrease(eventId)
        check(success) { "Inventory is empty" }

        try {
            ticketEventProducer.publish(eventPayload)
        } catch (e: Exception) {
            log.error("Failed to publish reservation event. Rolling back inventory for event: $eventId", e)
            inventoryManager.increase(eventId)
            throw e
        }

        return requestId
    }
}