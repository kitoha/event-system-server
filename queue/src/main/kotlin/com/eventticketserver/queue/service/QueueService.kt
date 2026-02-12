package com.eventticketserver.queue.service

import kotlinx.coroutines.flow.Flow

interface QueueService {

    /**
     * 대기열에 진입
     * @return 대기 순번 정보
     */
    fun enter(eventId: Long, userId: Long): QueuePosition

    /**
     * 현재 대기 순번 조회
     * @return 대기 순번 정보
     */
    fun getPosition(eventId: Long, userId: Long): QueuePosition

    /**
     * 실시간 대기 순번 스트림 (SSE용)
     * @return 대기 순번 정보 Flow
     */
    fun streamPosition(eventId: Long, userId: Long): Flow<QueuePosition>
}
