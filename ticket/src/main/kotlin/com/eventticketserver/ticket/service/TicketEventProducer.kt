package com.eventticketserver.ticket.service

import com.eventticketserver.common.event.TicketReservedEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class TicketEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, TicketReservedEvent>
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val TOPIC = "ticket-reserved"
    }

    fun publish(event: TicketReservedEvent) {
        kafkaTemplate.send(TOPIC, event.eventId.toString(), event)
            .whenComplete { _, ex ->
                if (ex == null) {
                    log.info("Published event: {} to topic: {}", event.requestId, TOPIC)
                } else {
                    log.error("Failed to publish event: {}", event.requestId, ex)
                }
            }
    }
}
