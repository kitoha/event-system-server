package com.eventticketserver.ticket.service

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component

@Component
class RedisInventoryManager(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val decreaseInventoryScript: RedisScript<Long>
) : InventoryManager {

    companion object {
        private const val INVENTORY_KEY_PREFIX = "event:inventory:"
    }

    override fun decrease(eventId: Long): Boolean {
        val key = "$INVENTORY_KEY_PREFIX$eventId"
        
        val result = redisTemplate.execute(decreaseInventoryScript, listOf(key))
        return result == 1L
    }
}