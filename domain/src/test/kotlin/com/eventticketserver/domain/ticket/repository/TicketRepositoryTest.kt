package com.eventticketserver.domain.ticket.repository

import com.eventticketserver.common.IntegrationTestSupportMarker
import com.eventticketserver.DomainTestApplication
import com.eventticketserver.domain.event.entity.Event
import com.eventticketserver.domain.event.repository.EventRepository
import com.eventticketserver.domain.ticket.entity.Ticket
import com.eventticketserver.domain.ticket.entity.TicketStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest(classes = [DomainTestApplication::class])
@Transactional
class TicketRepositoryTest(
    private val ticketRepository: TicketRepository,
    private val eventRepository: EventRepository
) : FunSpec({

    extension(SpringExtension)

    val defaultUserId = 100L
    val eventTitle = "IU Concert 2026"
    val totalSeats = 50000
    val pastOpenAt = LocalDateTime.now().minusDays(1)

    test("Ticket을 저장하고 조회할 수 있어야 한다") {
        // Given
        val event = eventRepository.save(Event.create(eventTitle, totalSeats, pastOpenAt))
        
        // When
        val ticket = Ticket.reserve(event, defaultUserId)
        val savedTicket = ticketRepository.save(ticket)

        // Then
        val foundTicket = ticketRepository.findById(savedTicket.id!!).get()
        foundTicket.userId shouldBe defaultUserId
        foundTicket.event.id shouldBe event.id
        foundTicket.status shouldBe TicketStatus.PENDING
    }

    test("특정 사용자의 티켓 목록을 조회할 수 있어야 한다") {
        // Given
        val event = eventRepository.save(Event.create(eventTitle, totalSeats, pastOpenAt))
        ticketRepository.save(Ticket.reserve(event, defaultUserId))
        ticketRepository.save(Ticket.reserve(event, defaultUserId))

        // When
        val tickets = ticketRepository.findAllByUserId(defaultUserId)

        // Then
        tickets.size shouldBe 2
        tickets.all { it.userId == defaultUserId } shouldBe true
    }
}), IntegrationTestSupportMarker
