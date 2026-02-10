package com.eventticketserver.queue.service

import org.springframework.stereotype.Service
import java.util.*

@Service
class QueueTokenService {

    /**
     * 대기열 토큰 생성
     * 현재는 UUID 기반 (간단한 구현)
     */
    fun generateToken(eventId: Long, userId: Long): String {
        // TODO: JWT로 변경 (eventId, userId, timestamp 포함)
        return "queue-token-${UUID.randomUUID()}"
    }

    /**
     * 토큰 검증
     * TODO: JWT 검증 로직 추가
     */
    fun validateToken(token: String): Boolean {
        return token.startsWith("queue-token-")
    }
}
