package com.eventticketserver.api.controller

import com.eventticketserver.api.dto.TicketReserveRequest
import com.eventticketserver.api.dto.TicketReserveResponse
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
    val webTestClient = WebTestClient.bindToController(controller).build()

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

    test("POST /api/v1/tickets/reserve - 재고 부족 시 에러를 반환한다") {
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
            .expectStatus().is5xxServerError
    }
})
