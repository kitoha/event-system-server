package com.eventticketserver.domain.event.entity

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class EventTest : FunSpec({

    val validTitle = "IU Concert 2026"
    val validTotalSeats = 100
    val futureOpenAt = LocalDateTime.now().plusDays(1)

    test("Event should be created successfully with valid parameters") {
        val event = Event.create(validTitle, validTotalSeats, futureOpenAt)

        event.title shouldBe validTitle
        event.totalSeats shouldBe validTotalSeats
        event.openAt shouldBe futureOpenAt
    }

    test("Event creation should fail if title is blank") {
        val blankTitle = " "
        
        val exception = shouldThrow<IllegalArgumentException> {
            Event.create(blankTitle, validTotalSeats, futureOpenAt)
        }
        
        exception.message shouldBe "Event title cannot be empty"
    }

    test("Event creation should fail if total seats is zero or negative") {
        val invalidSeats = 0
        
        val exception = shouldThrow<IllegalArgumentException> {
            Event.create(validTitle, invalidSeats, futureOpenAt)
        }
        
        exception.message shouldBe "Total seats must be greater than zero"
    }
})
