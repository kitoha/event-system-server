package com.eventticketserver.domain.event.repository

import com.eventticketserver.domain.event.entity.Event
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EventRepository : JpaRepository<Event, Long>
