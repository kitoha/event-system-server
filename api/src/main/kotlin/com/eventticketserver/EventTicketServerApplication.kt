package com.eventticketserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EventTicketServerApplication

fun main(args: Array<String>) {
    runApplication<EventTicketServerApplication>(*args)
}