package com.eventticketserver.domain.event.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "events")
class Event private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    val totalSeats: Int,

    @Column(nullable = false)
    val openAt: LocalDateTime,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {

    fun isOpen(now: LocalDateTime = LocalDateTime.now()): Boolean {
        return now.isAfter(openAt)
    }

    companion object {
        fun create(title: String, totalSeats: Int, openAt: LocalDateTime): Event {
            require(title.isNotBlank()) { "Event title cannot be empty" }
            require(totalSeats > 0) { "Total seats must be greater than zero" }
            
            return Event(
                title = title,
                totalSeats = totalSeats,
                openAt = openAt
            )
        }
    }
}
