package com.eventticketserver.domain.ticket.entity

enum class TicketStatus {
    PENDING,    // 결제 대기 중
    CONFIRMED,  // 예약 완료
    CANCELLED   // 취소됨
}
