package com.eventticketserver.domain.ticket.repository

import com.eventticketserver.domain.ticket.entity.Ticket
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TicketRepository : JpaRepository<Ticket, Long> {
    fun findAllByUserId(userId: Long): List<Ticket>
    fun existsByRequestId(requestId: String): Boolean
}
