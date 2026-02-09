package com.eventticketserver.ticket.service

import com.eventticketserver.domain.event.entity.Event
import com.eventticketserver.domain.event.repository.EventRepository
import com.eventticketserver.domain.ticket.entity.Ticket
import com.eventticketserver.domain.ticket.repository.TicketRepository
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
    val ticketRepository = mockk<TicketRepository>()
    val inventoryManager = mockk<InventoryManager>()
    val ticketingService = TicketingService(eventRepository, ticketRepository, inventoryManager)

    beforeTest {
        clearMocks(eventRepository, ticketRepository, inventoryManager)
    }

    val eventId = 1L
    val userId = 100L
    
    test("티켓 예약 성공: 이벤트가 존재하고 재고가 충분하면 티켓이 발행된다") {
        val event = Event.create("Test Event", 100, LocalDateTime.now().minusDays(1))
        every { eventRepository.findById(eventId) } returns Optional.of(event)
        every { inventoryManager.decrease(eventId) } returns true
        every { ticketRepository.save(any<Ticket>()) } returns mockk<Ticket>()

        ticketingService.reserve(eventId, userId)

        verify(exactly = 1) { inventoryManager.decrease(eventId) }
        verify(exactly = 1) { ticketRepository.save(any<Ticket>()) }
    }

    test("티켓 예약 실패: 재고가 없으면 IllegalStateException이 발생한다") {
        val event = Event.create("Test Event", 100, LocalDateTime.now().minusDays(1))
        every { eventRepository.findById(eventId) } returns Optional.of(event)
        every { inventoryManager.decrease(eventId) } returns false

        val exception = shouldThrow<IllegalStateException> {
            ticketingService.reserve(eventId, userId)
        }
        exception.message shouldBe "Inventory is empty"
        
        verify(exactly = 1) { inventoryManager.decrease(eventId) }
        verify(exactly = 0) { ticketRepository.save(any()) }
    }

    test("티켓 예약 실패: 이벤트가 존재하지 않으면 IllegalArgumentException이 발생한다") {
        every { eventRepository.findById(eventId) } returns Optional.empty()

        val exception = shouldThrow<IllegalArgumentException> {
            ticketingService.reserve(eventId, userId)
        }
        exception.message shouldBe "Event not found with id: $eventId"
        
        verify(exactly = 0) { inventoryManager.decrease(any()) }
    }
})
