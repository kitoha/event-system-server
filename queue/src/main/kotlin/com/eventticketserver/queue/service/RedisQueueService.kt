package com.eventticketserver.queue.service

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class RedisQueueService(
    private val redisTemplate: RedisTemplate<String, String>
) : QueueService {

    companion object {
        private const val QUEUE_KEY_PREFIX = "queue:event:"
        private const val ESTIMATED_WAIT_PER_USER_SECONDS = 1
        private const val STREAM_INTERVAL_MILLIS = 1000L
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

    override fun streamPosition(eventId: Long, userId: Long): Flow<QueuePosition> = flow {
        while (true) {
            try {
                val position = getPosition(eventId, userId)
                emit(position)

                // 순번이 1이면 대기열 처리 완료
                if (position.position == 1) {
                    break
                }

                delay(STREAM_INTERVAL_MILLIS)
            } catch (e: IllegalStateException) {
                // 사용자가 대기열에서 제거된 경우 스트림 종료
                break
            }
        }
    }
}
