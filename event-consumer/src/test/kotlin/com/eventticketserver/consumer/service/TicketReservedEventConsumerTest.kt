package com.eventticketserver.consumer.service

import com.eventticketserver.common.event.TicketReservedEvent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.mockk.*
import org.springframework.dao.DataIntegrityViolationException
import java.time.LocalDateTime
import java.util.*

class TicketReservedEventConsumerTest : FunSpec({

    val ticketService = mockk<TicketService>()
    val consumer = TicketReservedEventConsumer(ticketService)

    beforeTest {
        clearMocks(ticketService)
    }

    val userId = 100L
    val requestId = UUID.randomUUID().toString()

    test("이벤트를 수신하면 티켓을 정상적으로 저장한다") {
        // Given
        val ticketReservedEvent = TicketReservedEvent.newBuilder()
            .setEventId(1L)
            .setUserId(userId)
            .setRequestId(requestId)
            .setCreatedAt(LocalDateTime.now().toString())
            .build()

        every { ticketService.saveTicketFromEvent(ticketReservedEvent) } just Runs

        // When
        consumer.consume(ticketReservedEvent)

        // Then
        verify(exactly = 1) { ticketService.saveTicketFromEvent(ticketReservedEvent) }
    }

    test("저장 시 중복 키 예외(동시성 이슈)가 발생해도 정상 종료한다") {
        // Given
        val ticketReservedEvent = TicketReservedEvent.newBuilder()
            .setEventId(1L)
            .setUserId(userId)
            .setRequestId(requestId)
            .setCreatedAt(LocalDateTime.now().toString())
            .build()

        every { ticketService.saveTicketFromEvent(ticketReservedEvent) } throws DataIntegrityViolationException("Duplicate")

        // When
        consumer.consume(ticketReservedEvent)

        // Then
        verify(exactly = 1) { ticketService.saveTicketFromEvent(ticketReservedEvent) }
    }

    test("존재하지 않는 이벤트에 대한 요청이면 예외를 던진다") {
        // Given
        val ticketReservedEvent = TicketReservedEvent.newBuilder()
            .setEventId(1L)
            .setUserId(userId)
            .setRequestId(requestId)
            .setCreatedAt(LocalDateTime.now().toString())
            .build()

        every { ticketService.saveTicketFromEvent(ticketReservedEvent) } throws IllegalArgumentException("Event not found: 1")

        // When & Then
        shouldThrow<IllegalArgumentException> {
            consumer.consume(ticketReservedEvent)
        }
    }
})