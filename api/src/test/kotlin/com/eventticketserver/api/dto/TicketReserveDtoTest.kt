package com.eventticketserver.api.dto

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class TicketReserveDtoTest : FunSpec({

    test("TicketReserveRequest는 유효한 데이터로 객체를 생성할 수 있다") {
        // Given & When
        val request = TicketReserveRequest(
            eventId = 1L,
            userId = 100L,
            quantity = 2
        )

        // Then
        request.eventId shouldBe 1L
        request.userId shouldBe 100L
        request.quantity shouldBe 2
    }

    test("TicketReserveRequest의 quantity 기본값은 1이다") {
        // Given & When
        val request = TicketReserveRequest(
            eventId = 1L,
            userId = 100L
        )

        // Then
        request.quantity shouldBe 1
    }

    test("TicketReserveResponse는 필수 필드를 모두 포함한다") {
        // Given & When
        val response = TicketReserveResponse(
            reservationId = "req-123",
            status = "PENDING",
            message = "Ticket reservation is being processed"
        )

        // Then
        response.reservationId shouldBe "req-123"
        response.status shouldBe "PENDING"
        response.message shouldNotBe null
    }
})
