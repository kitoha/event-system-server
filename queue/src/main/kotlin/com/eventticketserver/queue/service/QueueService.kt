package com.eventticketserver.queue.service

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
}
