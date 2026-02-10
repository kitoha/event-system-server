package com.eventticketserver.ticket.service

import com.eventticketserver.common.event.TicketReservedEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class TicketEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, TicketReservedEvent>,
    @Value("\${app.kafka.topics.ticket-reserved:ticket-reserved}")
    private val topic: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun publish(event: TicketReservedEvent) {
        kafkaTemplate.send(topic, event.eventId.toString(), event)
            .whenComplete { _, ex ->
                if (ex == null) {
                    log.info("Published event: {} to topic: {}", event.requestId, topic)
                } else {
                    log.error("Failed to publish event: {}", event.requestId, ex)
                }
            }
    }
}
