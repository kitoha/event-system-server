package com.eventticketserver

import com.eventticketserver.common.IntegrationTestSupport
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(IntegrationTestSupport::class)
class EventTicketServerApplicationTests {

    @Test
    fun contextLoads() {
        // This test will now start PostgreSQL, Redis, and Kafka containers automatically
    }

}
