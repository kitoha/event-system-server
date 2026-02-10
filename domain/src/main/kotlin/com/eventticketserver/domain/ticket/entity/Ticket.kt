package com.eventticketserver.domain.ticket.entity

import com.eventticketserver.domain.event.entity.Event
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "tickets")
class Ticket private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    val event: Event,

    @Column(nullable = false)
    val userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: TicketStatus,

    @Column(name = "request_id", unique = true)
    val requestId: String? = null,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {

    companion object {
        fun reserve(event: Event, userId: Long, requestId: String? = null): Ticket {
            check(event.isOpen()) { "Ticket reservation is not available yet" }

            return Ticket(
                event = event,
                userId = userId,
                status = TicketStatus.PENDING,
                requestId = requestId
            )
        }
    }

    fun confirm() {
        this.status = TicketStatus.CONFIRMED
        this.updatedAt = LocalDateTime.now()
    }

    fun cancel() {
        this.status = TicketStatus.CANCELLED
        this.updatedAt = LocalDateTime.now()
    }
}
