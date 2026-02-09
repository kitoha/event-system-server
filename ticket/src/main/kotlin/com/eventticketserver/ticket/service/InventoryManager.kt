package com.eventticketserver.ticket.service

interface InventoryManager {
    /**
     * 재고를 하나 차감한다.
     * @return 차감 성공 여부 (재고가 없으면 false)
     */
    fun decrease(eventId: Long): Boolean

    /**
     * 재고를 하나 증가시킨다
     */
    fun increase(eventId: Long)
}