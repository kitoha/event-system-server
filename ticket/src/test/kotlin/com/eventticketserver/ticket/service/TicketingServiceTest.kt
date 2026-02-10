package com.eventticketserver.ticket.service

import com.eventticketserver.common.event.TicketReservedEvent
import com.eventticketserver.domain.event.entity.Event
import com.eventticketserver.domain.event.repository.EventRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime
import java.util.*

class TicketingServiceTest : FunSpec({

    val eventRepository = mockk<EventRepository>()
    val inventoryManager = mockk<InventoryManager>()
    val ticketEventProducer = mockk<TicketEventProducer>()
    val ticketingService = TicketingService(eventRepository, inventoryManager, ticketEventProducer)

    beforeTest {
        clearMocks(eventRepository, inventoryManager, ticketEventProducer)
    }

    val eventId = 1L
    val userId = 100L
    
    test("티켓 예약 성공: 이벤트가 오픈되어 있고 재고가 충분하면 예약 이벤트가 발행된다") {
        // Given: 오픈된 이벤트 실제 생성
        val pastOpenAt = LocalDateTime.now().minusHours(1)
        val event = Event.create("Test Event", 100, pastOpenAt)
        
        every { eventRepository.findById(eventId) } returns Optional.of(event)
        every { inventoryManager.decrease(eventId) } returns true
        every { ticketEventProducer.publish(any()) } returns Unit

        // When
        ticketingService.reserve(eventId, userId)

        // Then
        verify(exactly = 1) { inventoryManager.decrease(eventId) }
        verify(exactly = 1) { ticketEventProducer.publish(any<TicketReservedEvent>()) }
    }

    test("티켓 예약 실패: 이벤트가 아직 오픈되지 않았으면 IllegalStateException이 발생한다") {
        // Given: 미래에 오픈될 이벤트 실제 생성
        val futureOpenAt = LocalDateTime.now().plusHours(1)
        val event = Event.create("Future Event", 100, futureOpenAt)
        
        every { eventRepository.findById(eventId) } returns Optional.of(event)

        // When & Then
        val exception = shouldThrow<IllegalStateException> {
            ticketingService.reserve(eventId, userId)
        }
        exception.message shouldBe "Ticket reservation is not available yet"
        
        verify(exactly = 0) { inventoryManager.decrease(any()) }
        verify(exactly = 0) { ticketEventProducer.publish(any()) }
    }

    test("티켓 예약 실패: 재고가 없으면 IllegalStateException이 발생한다") {
        // Given: 오픈된 이벤트 실제 생성
        val pastOpenAt = LocalDateTime.now().minusHours(1)
        val event = Event.create("Test Event", 100, pastOpenAt)
        
        every { eventRepository.findById(eventId) } returns Optional.of(event)
        every { inventoryManager.decrease(eventId) } returns false

        // When & Then
        val exception = shouldThrow<IllegalStateException> {
            ticketingService.reserve(eventId, userId)
        }
        exception.message shouldBe "Inventory is empty"
        
        verify(exactly = 1) { inventoryManager.decrease(eventId) }
        verify(exactly = 0) { ticketEventProducer.publish(any()) }
    }

    test("티켓 예약 실패: 이벤트 발행 중 예외가 발생하면 Redis 재고가 복구되어야 한다") {
        // Given: 오픈된 이벤트 실제 생성
        val pastOpenAt = LocalDateTime.now().minusHours(1)
        val event = Event.create("Test Event", 100, pastOpenAt)
        
        every { eventRepository.findById(eventId) } returns Optional.of(event)
        every { inventoryManager.decrease(eventId) } returns true
        every { inventoryManager.increase(eventId) } returns Unit
        every { ticketEventProducer.publish(any()) } throws RuntimeException("Kafka Error")

        // When & Then
        shouldThrow<RuntimeException> {
            ticketingService.reserve(eventId, userId)
        }

        verify(exactly = 1) { inventoryManager.decrease(eventId) }
        verify(exactly = 1) { inventoryManager.increase(eventId) }
    }
})