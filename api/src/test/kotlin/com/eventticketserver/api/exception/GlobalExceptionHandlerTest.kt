package com.eventticketserver.api.exception

import com.eventticketserver.api.dto.QueueEnterRequest
import com.eventticketserver.queue.service.QueueService
import com.eventticketserver.queue.service.QueueTokenService
import com.eventticketserver.api.controller.QueueController
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

class GlobalExceptionHandlerTest : FunSpec({

    val queueService = mockk<QueueService>()
    val queueTokenService = mockk<QueueTokenService>()
    val controller = QueueController(queueService, queueTokenService)
    val exceptionHandler = GlobalExceptionHandler()

    val webTestClient = WebTestClient.bindToController(controller)
        .controllerAdvice(exceptionHandler)
        .build()

    test("IllegalArgumentException 발생 시 400 Bad Request를 반환한다") {
        // Given
        val request = QueueEnterRequest(eventId = 1L, userId = 100L)
        every { queueService.enter(1L, 100L) } throws IllegalArgumentException("Invalid event ID")

        // When & Then
        webTestClient.post()
            .uri("/api/v1/queue/enter")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.status").isEqualTo(400)
            .jsonPath("$.error").isEqualTo("Bad Request")
            .jsonPath("$.message").isEqualTo("Invalid event ID")
            .jsonPath("$.path").isEqualTo("/api/v1/queue/enter")
            .jsonPath("$.timestamp").isNotEmpty
    }

    test("IllegalStateException 발생 시 409 Conflict를 반환한다") {
        // Given
        val request = QueueEnterRequest(eventId = 1L, userId = 100L)
        every { queueService.enter(1L, 100L) } throws IllegalStateException("Queue is full")

        // When & Then
        webTestClient.post()
            .uri("/api/v1/queue/enter")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isEqualTo(409)
            .expectBody()
            .jsonPath("$.status").isEqualTo(409)
            .jsonPath("$.error").isEqualTo("Conflict")
            .jsonPath("$.message").isEqualTo("Queue is full")
            .jsonPath("$.path").isEqualTo("/api/v1/queue/enter")
            .jsonPath("$.timestamp").isNotEmpty
    }

    test("일반 Exception 발생 시 500 Internal Server Error를 반환한다") {
        // Given
        val request = QueueEnterRequest(eventId = 1L, userId = 100L)
        every { queueService.enter(1L, 100L) } throws RuntimeException("Unexpected error")

        // When & Then
        webTestClient.post()
            .uri("/api/v1/queue/enter")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isEqualTo(500)
            .expectBody()
            .jsonPath("$.status").isEqualTo(500)
            .jsonPath("$.error").isEqualTo("Internal Server Error")
            .jsonPath("$.message").isEqualTo("Unexpected error")
            .jsonPath("$.path").isEqualTo("/api/v1/queue/enter")
            .jsonPath("$.timestamp").isNotEmpty
    }
})
