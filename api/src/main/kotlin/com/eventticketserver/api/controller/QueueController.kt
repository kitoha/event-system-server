package com.eventticketserver.api.controller

import com.eventticketserver.api.dto.QueueEnterRequest
import com.eventticketserver.api.dto.QueueEnterResponse
import com.eventticketserver.api.dto.QueuePositionResponse
import com.eventticketserver.queue.service.QueueService
import com.eventticketserver.queue.service.QueueTokenService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/queue")
class QueueController(
    private val queueService: QueueService,
    private val queueTokenService: QueueTokenService
) {

    @PostMapping("/enter")
    fun enter(
        @RequestBody request: QueueEnterRequest
    ): QueueEnterResponse {
        val position = queueService.enter(request.eventId, request.userId)
        val token = queueTokenService.generateToken(request.eventId, request.userId)

        return QueueEnterResponse(
            token = token,
            position = position.position,
            estimatedWaitSeconds = position.estimatedWaitSeconds
        )
    }

    @GetMapping("/position")
    fun getPosition(
        @RequestParam eventId: Long,
        @RequestParam userId: Long
    ): QueuePositionResponse {
        val position = queueService.getPosition(eventId, userId)

        return QueuePositionResponse(
            position = position.position,
            estimatedWaitSeconds = position.estimatedWaitSeconds
        )
    }
}
