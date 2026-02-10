package com.eventticketserver.api.dto

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual

class QueueDtoTest : FunSpec({

    test("QueueEnterRequest는 유효한 데이터로 객체를 생성할 수 있다") {
        // Given & When
        val request = QueueEnterRequest(
            eventId = 1L,
            userId = 100L
        )

        // Then
        request.eventId shouldBe 1L
        request.userId shouldBe 100L
    }

    test("QueueEnterResponse는 토큰과 대기 정보를 포함한다") {
        // Given & When
        val response = QueueEnterResponse(
            token = "jwt-token-123",
            position = 1523,
            estimatedWaitSeconds = 180
        )

        // Then
        response.token shouldBe "jwt-token-123"
        response.position shouldBe 1523
        response.estimatedWaitSeconds shouldBe 180
    }

    test("QueueEnterResponse의 대기 시간은 0 이상이다") {
        // Given & When
        val response = QueueEnterResponse(
            token = "jwt-token-123",
            position = 1,
            estimatedWaitSeconds = 0
        )

        // Then
        response.estimatedWaitSeconds shouldBeGreaterThanOrEqual 0
    }

    test("QueuePositionResponse는 순번과 예상 대기 시간을 포함한다") {
        // Given & When
        val response = QueuePositionResponse(
            position = 1420,
            estimatedWaitSeconds = 165
        )

        // Then
        response.position shouldBe 1420
        response.estimatedWaitSeconds shouldBe 165
    }
})
