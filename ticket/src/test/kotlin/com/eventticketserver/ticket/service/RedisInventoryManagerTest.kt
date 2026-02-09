package com.eventticketserver.ticket.service

import com.eventticketserver.common.IntegrationTestSupport
import com.eventticketserver.TicketTestApplication
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.RedisTemplate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest(classes = [TicketTestApplication::class])
@Import(IntegrationTestSupport::class)
class RedisInventoryManagerTest(
    private val inventoryManager: RedisInventoryManager,
    private val redisTemplate: RedisTemplate<String, Any>
) : FunSpec({

    extension(SpringExtension)

    val eventId = 1L
    val inventoryKey = "event:inventory:$eventId"
    val initialStock = 100

    test("동시성 테스트: 150명이 동시에 100개의 재고를 차감하면 정확히 100명만 성공해야 한다") {
        // Given
        redisTemplate.opsForValue().set(inventoryKey, initialStock.toString())
        
        val threadCount = 150
        val executorService = Executors.newFixedThreadPool(32)
        val latch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)

        try {
            // When
            for (i in 0 until threadCount) {
                executorService.execute {
                    try {
                        val result = inventoryManager.decrease(eventId)
                        if (result) successCount.incrementAndGet()
                        else failCount.incrementAndGet()
                    } finally {
                        latch.countDown()
                    }
                }
            }

            // Wait with timeout (e.g., 10 seconds)
            val completed = latch.await(10, TimeUnit.SECONDS)
            if (!completed) {
                throw RuntimeException("Test timed out waiting for threads to finish")
            }

            // Then
            successCount.get() shouldBe initialStock
            failCount.get() shouldBe (threadCount - initialStock)
            
            val remainingStock = redisTemplate.opsForValue().get(inventoryKey).toString().toInt()
            remainingStock shouldBe 0
        } finally {
            // Cleanup
            executorService.shutdown()
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow()
                }
            } catch (e: InterruptedException) {
                executorService.shutdownNow()
                Thread.currentThread().interrupt()
            }
            redisTemplate.delete(inventoryKey)
        }
    }
})
