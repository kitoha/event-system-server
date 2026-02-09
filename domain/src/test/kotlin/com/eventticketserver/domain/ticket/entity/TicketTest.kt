package com.eventticketserver.domain.ticket.entity

import com.eventticketserver.domain.event.entity.Event
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class TicketTest : FunSpec({

    val userId = 1L
    
    test("Ticket should be created successfully after event open time") {
        // Given:
        val pastOpenAt = LocalDateTime.now().minusHours(1)
        val openedEvent = Event.create("Opened Event", 100, pastOpenAt)

        // When
        val ticket = Ticket.reserve(openedEvent, userId)

        // Then
        ticket.event shouldBe openedEvent
        ticket.userId shouldBe userId
        ticket.status shouldBe TicketStatus.PENDING
    }

    test("Ticket reservation should fail if event is not yet open") {
        // Given:
        val futureOpenAt = LocalDateTime.now().plusHours(1)
        val futureEvent = Event.create("Future Event", 100, futureOpenAt)

        // When & Then
        val exception = shouldThrow<IllegalStateException> {
            Ticket.reserve(futureEvent, userId)
        }
        
        exception.message shouldBe "Ticket reservation is not available yet"
    }

    test("Ticket status can be changed to CONFIRMED") {
        // Given
        val pastOpenAt = LocalDateTime.now().minusHours(1)
        val openedEvent = Event.create("Opened Event", 100, pastOpenAt)
        val ticket = Ticket.reserve(openedEvent, userId)

        // When
        ticket.confirm()

        // Then
        ticket.status shouldBe TicketStatus.CONFIRMED
    }

    test("Ticket status can be changed to CANCELLED") {
        // Given
        val pastOpenAt = LocalDateTime.now().minusHours(1)
        val openedEvent = Event.create("Opened Event", 100, pastOpenAt)
        val ticket = Ticket.reserve(openedEvent, userId)

        // When
        ticket.cancel()

        // Then
        ticket.status shouldBe TicketStatus.CANCELLED
    }
})
