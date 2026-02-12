package com.eventticketserver.queue.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.*
import kotlinx.coroutines.flow.toList
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ZSetOperations

class RedisQueueServiceTest : FunSpec({

    val redisTemplate = mockk<RedisTemplate<String, String>>()
    val zSetOps = mockk<ZSetOperations<String, String>>()
    val queueService = RedisQueueService(redisTemplate)

    beforeTest {
        clearMocks(redisTemplate, zSetOps)
        every { redisTemplate.opsForZSet() } returns zSetOps
    }

    test("대기열에 처음 진입하면 순번을 받는다") {
        // Given
        val eventId = 1L
        val userId = 100L
        val key = "queue:event:1"

        every { zSetOps.add(key, "user:100", any()) } returns true
        every { zSetOps.rank(key, "user:100") } returns 0L
        every { zSetOps.size(key) } returns 1L

        // When
        val result = queueService.enter(eventId, userId)

        // Then
        result.position shouldBe 1
        result.estimatedWaitSeconds shouldBe 0

        verify(exactly = 1) { zSetOps.add(key, "user:100", any()) }
    }

    test("이미 대기 중인 사용자가 재진입하면 기존 위치를 반환한다") {
        // Given
        val eventId = 1L
        val userId = 100L
        val key = "queue:event:1"

        every { zSetOps.add(key, "user:100", any()) } returns false
        every { zSetOps.rank(key, "user:100") } returns 5L
        every { zSetOps.size(key) } returns 10L

        // When
        val result = queueService.enter(eventId, userId)

        // Then
        result.position shouldBe 6
    }

    test("대기열에 여러 사용자가 있으면 순번이 증가한다") {
        // Given
        val eventId = 1L
        val key = "queue:event:1"

        every { zSetOps.add(key, "user:100", any()) } returns true
        every { zSetOps.rank(key, "user:100") } returns 0L
        every { zSetOps.size(key) } returns 1L

        val first = queueService.enter(eventId, 100L)

        every { zSetOps.add(key, "user:200", any()) } returns true
        every { zSetOps.rank(key, "user:200") } returns 1L
        every { zSetOps.size(key) } returns 2L

        val second = queueService.enter(eventId, 200L)

        // Then
        first.position shouldBe 1
        second.position shouldBe 2
    }

    test("현재 대기 순번을 조회할 수 있다") {
        // Given
        val eventId = 1L
        val userId = 100L
        val key = "queue:event:1"

        every { zSetOps.rank(key, "user:100") } returns 10L
        every { zSetOps.size(key) } returns 100L

        // When
        val result = queueService.getPosition(eventId, userId)

        // Then
        result.position shouldBe 11
        result.estimatedWaitSeconds shouldBeGreaterThan 0
    }

    test("대기열에 없는 사용자가 조회하면 예외가 발생한다") {
        // Given
        val eventId = 1L
        val userId = 999L
        val key = "queue:event:1"

        every { zSetOps.rank(key, "user:999") } returns null

        // When & Then
        val exception = runCatching {
            queueService.getPosition(eventId, userId)
        }.exceptionOrNull()

        exception.shouldBeInstanceOf<IllegalStateException>()
    }

    test("streamPosition - 순번이 감소하면서 스트리밍되다가 1이 되면 종료한다") {
        // Given
        val eventId = 1L
        val userId = 100L
        val key = "queue:event:1"

        every { zSetOps.rank(key, "user:100") } returnsMany listOf(2L, 1L, 0L)

        // When
        val positions = queueService.streamPosition(eventId, userId).toList()

        // Then
        positions.size shouldBe 3
        positions[0].position shouldBe 3
        positions[0].estimatedWaitSeconds shouldBe 2
        positions[1].position shouldBe 2
        positions[1].estimatedWaitSeconds shouldBe 1
        positions[2].position shouldBe 1
        positions[2].estimatedWaitSeconds shouldBe 0

        verify(atLeast = 3) { zSetOps.rank(key, "user:100") }
    }

    test("streamPosition - 사용자가 대기열에서 제거되면 스트림이 종료한다") {
        // Given
        val eventId = 1L
        val userId = 100L
        val key = "queue:event:1"

        every { zSetOps.rank(key, "user:100") } returnsMany listOf(2L, 1L, null)

        // When
        val positions = queueService.streamPosition(eventId, userId).toList()

        // Then
        positions.size shouldBe 2
        positions[0].position shouldBe 3
        positions[1].position shouldBe 2

        verify(atLeast = 2) { zSetOps.rank(key, "user:100") }
    }
})
