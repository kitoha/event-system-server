package com.eventticketserver.consumer.service

import com.eventticketserver.common.event.TicketReservedEvent
import com.eventticketserver.domain.event.repository.EventRepository
import com.eventticketserver.domain.ticket.entity.Ticket
import com.eventticketserver.domain.ticket.repository.TicketRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TicketReservedEventConsumer(
    private val ticketRepository: TicketRepository,
    private val eventRepository: EventRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["\${app.kafka.topics.ticket-reserved:ticket-reserved}"],
        groupId = "\${spring.kafka.consumer.group-id:event-ticket-consumer-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    fun consume(event: TicketReservedEvent) {
        val requestId = event.requestId.toString()
        log.info("Received ticket reservation event: {}", requestId)

        try {
            if (ticketRepository.existsByRequestId(requestId)) {
                log.warn("Ticket already exists for requestId: {}. Skipping.", requestId)
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

        } catch (e: DataIntegrityViolationException) {
            log.warn("Duplicate ticket reservation detected during save: {}. Skipping.", requestId, e)
        } catch (e: Exception) {
            log.error("Failed to process event: {}", requestId, e)
            throw e
        }
    }
}
