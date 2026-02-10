package com.eventticketserver.api.controller

import com.eventticketserver.api.dto.TicketReserveRequest
import com.eventticketserver.api.exception.GlobalExceptionHandler
import com.eventticketserver.ticket.service.TicketingService
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

class TicketControllerTest : FunSpec({

    val ticketingService = mockk<TicketingService>()
    val controller = TicketController(ticketingService)
    val exceptionHandler = GlobalExceptionHandler()
    val webTestClient = WebTestClient.bindToController(controller)
        .controllerAdvice(exceptionHandler)
        .build()

    beforeTest {
        io.mockk.clearMocks(ticketingService)
    }

    test("POST /api/v1/tickets/reserve - 유효한 요청으로 티켓 예약에 성공한다") {
        // Given
        val request = TicketReserveRequest(
            eventId = 1L,
            userId = 100L,
            quantity = 2
        )

        every { ticketingService.reserve(1L, 100L) } returns "test-request-id-123"

        // When & Then
        webTestClient.post()
            .uri("/api/v1/tickets/reserve")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo("PENDING")
            .jsonPath("$.reservationId").isEqualTo("test-request-id-123")
            .jsonPath("$.message").isNotEmpty

        verify(exactly = 1) { ticketingService.reserve(1L, 100L) }
    }

    test("POST /api/v1/tickets/reserve - quantity 기본값은 1이다") {
        // Given
        val request = TicketReserveRequest(
            eventId = 1L,
            userId = 100L
        )

        every { ticketingService.reserve(1L, 100L) } returns "test-request-id-456"

        // When & Then
        webTestClient.post()
            .uri("/api/v1/tickets/reserve")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk

        verify(exactly = 1) { ticketingService.reserve(1L, 100L) }
    }

    test("POST /api/v1/tickets/reserve - 재고 부족 시 409 Conflict를 반환한다") {
        // Given
        val request = TicketReserveRequest(
            eventId = 1L,
            userId = 100L
        )

        every { ticketingService.reserve(1L, 100L) } throws IllegalStateException("Inventory is empty")

        // When & Then
        webTestClient.post()
            .uri("/api/v1/tickets/reserve")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isEqualTo(409)
            .expectBody()
            .jsonPath("$.status").isEqualTo(409)
            .jsonPath("$.error").isEqualTo("Conflict")
            .jsonPath("$.message").isEqualTo("Inventory is empty")
            .jsonPath("$.path").isEqualTo("/api/v1/tickets/reserve")
    }

    test("POST /api/v1/tickets/reserve - 존재하지 않는 이벤트 요청 시 400 Bad Request를 반환한다") {
        // Given
        val request = TicketReserveRequest(
            eventId = 999L,
            userId = 100L
        )

        every { ticketingService.reserve(999L, 100L) } throws IllegalArgumentException("Event not found")

        // When & Then
        webTestClient.post()
            .uri("/api/v1/tickets/reserve")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.status").isEqualTo(400)
            .jsonPath("$.error").isEqualTo("Bad Request")
            .jsonPath("$.message").isEqualTo("Event not found")
            .jsonPath("$.path").isEqualTo("/api/v1/tickets/reserve")
    }

    test("POST /api/v1/tickets/reserve - 이벤트가 아직 오픈되지 않았을 때 400 Bad Request를 반환한다") {
        // Given
        val request = TicketReserveRequest(
            eventId = 1L,
            userId = 100L
        )

        every { ticketingService.reserve(1L, 100L) } throws IllegalArgumentException("Not available yet")

        // When & Then
        webTestClient.post()
            .uri("/api/v1/tickets/reserve")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.status").isEqualTo(400)
            .jsonPath("$.error").isEqualTo("Bad Request")
            .jsonPath("$.message").isEqualTo("Not available yet")
            .jsonPath("$.path").isEqualTo("/api/v1/tickets/reserve")
    }
})
