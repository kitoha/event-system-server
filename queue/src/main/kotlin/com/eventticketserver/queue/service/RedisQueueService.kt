package com.eventticketserver.queue.service

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class RedisQueueService(
    private val redisTemplate: RedisTemplate<String, String>
) : QueueService {

    companion object {
        private const val QUEUE_KEY_PREFIX = "queue:event:"
        private const val ESTIMATED_WAIT_PER_USER_SECONDS = 1
    }

    override fun enter(eventId: Long, userId: Long): QueuePosition {
        val key = "$QUEUE_KEY_PREFIX$eventId"
        val member = "user:$userId"
        val timestamp = System.currentTimeMillis().toDouble()

        redisTemplate.opsForZSet().add(key, member, timestamp)

        return getPosition(eventId, userId)
    }

    override fun getPosition(eventId: Long, userId: Long): QueuePosition {
        val key = "$QUEUE_KEY_PREFIX$eventId"
        val member = "user:$userId"

        val rank = redisTemplate.opsForZSet().rank(key, member)
            ?: throw IllegalStateException("User not found in queue")

        val position = rank.toInt() + 1

        val estimatedWaitSeconds = (position - 1) * ESTIMATED_WAIT_PER_USER_SECONDS

        return QueuePosition(
            position = position,
            estimatedWaitSeconds = estimatedWaitSeconds
        )
    }
}
