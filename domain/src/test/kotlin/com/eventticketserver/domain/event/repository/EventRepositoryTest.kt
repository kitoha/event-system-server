package com.eventticketserver.domain.event.repository

import com.eventticketserver.common.IntegrationTestSupport
import com.eventticketserver.domain.DomainTestApplication
import com.eventticketserver.domain.event.entity.Event
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest(classes = [DomainTestApplication::class])
@Transactional
@Import(IntegrationTestSupport::class)
class EventRepositoryTest(
    private val eventRepository: EventRepository
) : StringSpec({

    val defaultTitle = "Spring Boot Performance Seminar"
    val defaultSeats = 50
    val futureOpenAt = LocalDateTime.now().plusDays(7)

    "Event를 저장하고 다시 조회할 수 있어야 한다" {
        // Given
        val event = Event.create(defaultTitle, defaultSeats, futureOpenAt)

        // When
        val savedEvent = eventRepository.save(event)

        // Then
        val foundEvent = eventRepository.findById(savedEvent.id!!).get()
        foundEvent.title shouldBe defaultTitle
        foundEvent.totalSeats shouldBe defaultSeats
        foundEvent.id shouldNotBe null
    }
}) {
    override fun extensions() = listOf(SpringExtension)
}