package com.eventticketserver.consumer.service

import com.eventticketserver.common.event.TicketReservedEvent
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class TicketReservedEventConsumer(
    private val ticketService: TicketService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["\${app.kafka.topics.ticket-reserved:ticket-reserved}"],
        groupId = "\${spring.kafka.consumer.group-id:event-ticket-consumer-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun consume(event: TicketReservedEvent) {
        val requestId = event.requestId.toString()
        log.info("Received ticket reservation event: {}", requestId)

        try {
            ticketService.saveTicketFromEvent(event)
        } catch (e: DataIntegrityViolationException) {
            log.warn("Duplicate ticket reservation detected during save: {}. Skipping.", requestId, e)
        } catch (e: Exception) {
            log.error("Failed to process event: {}", requestId, e)
            throw e
        }
    }
}
