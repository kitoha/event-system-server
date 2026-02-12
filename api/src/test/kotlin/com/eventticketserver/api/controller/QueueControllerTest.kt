package com.eventticketserver.api.controller

import com.eventticketserver.api.dto.QueueEnterRequest
import com.eventticketserver.api.exception.GlobalExceptionHandler
import com.eventticketserver.queue.service.QueuePosition
import com.eventticketserver.queue.service.QueueService
import com.eventticketserver.queue.service.QueueTokenService
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

class QueueControllerTest : FunSpec({

    val queueService = mockk<QueueService>()
    val queueTokenService = mockk<QueueTokenService>()
    val controller = QueueController(queueService, queueTokenService)
    val exceptionHandler = GlobalExceptionHandler()
    val webTestClient = WebTestClient.bindToController(controller)
        .controllerAdvice(exceptionHandler)
        .build()

    beforeTest {
        clearMocks(queueService, queueTokenService)
    }

    test("POST /api/v1/queue/enter - 대기열 진입에 성공한다") {
        // Given
        val request = QueueEnterRequest(
            eventId = 1L,
            userId = 100L
        )
        val position = QueuePosition(position = 1, estimatedWaitSeconds = 0)
        val token = "queue-token-abc123"

        every { queueService.enter(1L, 100L) } returns position
        every { queueTokenService.generateToken(1L, 100L) } returns token

        // When & Then
        webTestClient.post()
            .uri("/api/v1/queue/enter")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.token").isEqualTo(token)
            .jsonPath("$.position").isEqualTo(1)
            .jsonPath("$.estimatedWaitSeconds").isEqualTo(0)

        verify(exactly = 1) { queueService.enter(1L, 100L) }
        verify(exactly = 1) { queueTokenService.generateToken(1L, 100L) }
    }

    test("POST /api/v1/queue/enter - 여러 사용자가 진입하면 순번이 증가한다") {
        // Given
        val request1 = QueueEnterRequest(eventId = 1L, userId = 100L)
        val request2 = QueueEnterRequest(eventId = 1L, userId = 200L)

        val position1 = QueuePosition(position = 1, estimatedWaitSeconds = 0)
        val position2 = QueuePosition(position = 2, estimatedWaitSeconds = 1)

        every { queueService.enter(1L, 100L) } returns position1
        every { queueService.enter(1L, 200L) } returns position2
        every { queueTokenService.generateToken(any(), any()) } returns "queue-token-test"

        // When & Then
        webTestClient.post()
            .uri("/api/v1/queue/enter")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request1)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.position").isEqualTo(1)
            .jsonPath("$.estimatedWaitSeconds").isEqualTo(0)

        // When & Then
        webTestClient.post()
            .uri("/api/v1/queue/enter")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request2)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.position").isEqualTo(2)
            .jsonPath("$.estimatedWaitSeconds").isEqualTo(1)
    }

    test("GET /api/v1/queue/position - 현재 대기 순번을 조회한다") {
        // Given
        val position = QueuePosition(position = 5, estimatedWaitSeconds = 4)
        every { queueService.getPosition(1L, 100L) } returns position

        // When & Then
        webTestClient.get()
            .uri("/api/v1/queue/position?eventId=1&userId=100")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.position").isEqualTo(5)
            .jsonPath("$.estimatedWaitSeconds").isEqualTo(4)

        verify(exactly = 1) { queueService.getPosition(1L, 100L) }
    }

    test("GET /api/v1/queue/position - 대기열에 없는 사용자 조회 시 409 Conflict를 반환한다") {
        // Given
        every { queueService.getPosition(1L, 999L) } throws IllegalStateException("User not found in queue")

        // When & Then
        webTestClient.get()
            .uri("/api/v1/queue/position?eventId=1&userId=999")
            .exchange()
            .expectStatus().isEqualTo(409)
            .expectBody()
            .jsonPath("$.status").isEqualTo(409)
            .jsonPath("$.error").isEqualTo("Conflict")
            .jsonPath("$.message").isEqualTo("User not found in queue")
            .jsonPath("$.path").isEqualTo("/api/v1/queue/position")

        verify(exactly = 1) { queueService.getPosition(1L, 999L) }
    }

    test("GET /api/v1/queue/stream - SSE로 실시간 대기 순번을 스트리밍한다") {
        // Given
        val positions = flowOf(
            QueuePosition(position = 3, estimatedWaitSeconds = 2),
            QueuePosition(position = 2, estimatedWaitSeconds = 1),
            QueuePosition(position = 1, estimatedWaitSeconds = 0)
        )

        every { queueService.streamPosition(1L, 100L) } returns positions

        // When & Then
        val result = webTestClient.get()
            .uri("/api/v1/queue/stream?eventId=1&userId=100")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
            .returnResult(com.eventticketserver.api.dto.QueuePositionResponse::class.java)

        val body = result.responseBody.collectList().block()!!

        // Then
        body.size shouldBe 3
        body[0].position shouldBe 3
        body[0].estimatedWaitSeconds shouldBe 2
        body[1].position shouldBe 2
        body[1].estimatedWaitSeconds shouldBe 1
        body[2].position shouldBe 1
        body[2].estimatedWaitSeconds shouldBe 0

        verify(exactly = 1) { queueService.streamPosition(1L, 100L) }
    }
})
