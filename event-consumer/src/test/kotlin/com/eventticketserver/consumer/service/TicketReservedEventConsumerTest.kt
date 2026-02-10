package com.eventticketserver.consumer.service

import com.eventticketserver.common.event.TicketReservedEvent
import com.eventticketserver.domain.event.entity.Event
import com.eventticketserver.domain.event.repository.EventRepository
import com.eventticketserver.domain.ticket.entity.Ticket
import com.eventticketserver.domain.ticket.repository.TicketRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.mockk.*
import org.springframework.dao.DataIntegrityViolationException
import java.time.LocalDateTime
import java.util.*

class TicketReservedEventConsumerTest : FunSpec({

    val ticketRepository = mockk<TicketRepository>()
    val eventRepository = mockk<EventRepository>()
    val consumer = TicketReservedEventConsumer(ticketRepository, eventRepository)

    beforeTest {
        clearMocks(ticketRepository, eventRepository)
    }

    val userId = 100L
    val requestId = UUID.randomUUID().toString()

    test("이벤트를 수신하면 티켓을 정상적으로 저장한다") {
        // Given
        val event = Event.create("Unit Test Event", 100, LocalDateTime.now().minusHours(1))
        val ticketReservedEvent = TicketReservedEvent.newBuilder()
            .setEventId(1L)
            .setUserId(userId)
            .setRequestId(requestId)
            .setCreatedAt(LocalDateTime.now().toString())
            .build()

        every { ticketRepository.existsByRequestId(requestId) } returns false
        every { eventRepository.findById(1L) } returns Optional.of(event)
        every { ticketRepository.save(any<Ticket>()) } returns mockk()

        // When
        consumer.consume(ticketReservedEvent)

        // Then
        verify(exactly = 1) { ticketRepository.existsByRequestId(requestId) }
        verify(exactly = 1) { ticketRepository.save(match { it.requestId == requestId }) }
    }

    test("이미 처리된 requestId인 경우(멱등성) 조기 종료한다") {
        // Given
        val ticketReservedEvent = TicketReservedEvent.newBuilder()
            .setEventId(1L)
            .setUserId(userId)
            .setRequestId(requestId)
            .setCreatedAt(LocalDateTime.now().toString())
            .build()

        every { ticketRepository.existsByRequestId(requestId) } returns true

        // When
        consumer.consume(ticketReservedEvent)

        // Then
        verify(exactly = 1) { ticketRepository.existsByRequestId(requestId) }
        verify(exactly = 0) { eventRepository.findById(any()) }
        verify(exactly = 0) { ticketRepository.save(any()) }
    }

    test("저장 시 중복 키 예외(동시성 이슈)가 발생해도 정상 종료한다") {
        // Given
        val event = Event.create("Race Condition Event", 100, LocalDateTime.now().minusHours(1))
        val ticketReservedEvent = TicketReservedEvent.newBuilder()
            .setEventId(1L)
            .setUserId(userId)
            .setRequestId(requestId)
            .setCreatedAt(LocalDateTime.now().toString())
            .build()

        every { ticketRepository.existsByRequestId(requestId) } returns false
        every { eventRepository.findById(1L) } returns Optional.of(event)
        every { ticketRepository.save(any()) } throws DataIntegrityViolationException("Duplicate")

        // When
        consumer.consume(ticketReservedEvent)

        // Then
        verify(exactly = 1) { ticketRepository.save(any()) }
    }

    test("존재하지 않는 이벤트에 대한 요청이면 예외를 던진다") {
        // Given
        val ticketReservedEvent = TicketReservedEvent.newBuilder()
            .setEventId(1L)
            .setUserId(userId)
            .setRequestId(requestId)
            .setCreatedAt(LocalDateTime.now().toString())
            .build()

        every { ticketRepository.existsByRequestId(requestId) } returns false
        every { eventRepository.findById(1L) } returns Optional.empty()

        // When & Then
        shouldThrow<IllegalArgumentException> {
            consumer.consume(ticketReservedEvent)
        }
    }
})