package com.bookk.core.data.eventstreaming.data.outbox

import com.bookk.core.data.eventstreaming.EventStreaming
import com.bookk.core.data.eventstreaming.OutboxEventDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class OutboxRelay(
    private val outboxEventDataSource: OutboxEventDataSource,
    private val transactionManager: TransactionManager,
    private val rawProducer: EventStreaming.RawProducer<String>,
    private val batchSize: Int = DEFAULT_BATCH_SIZE,
    private val idleDelay: Duration = DEFAULT_IDLE_DELAY
) {
    private val logger = KtorSimpleLogger("OutboxRelay")

    suspend fun relayBatch(): Result<Int> = transactionManager.transaction {
        val events = outboxEventDataSource.lockPending(batchSize)
        events.forEach { rawProducer.publish(it.topic, it.partitionKey, it.payload) }
        outboxEventDataSource.delete(events.map { it.id })
        events.size
    }

    fun start(scope: CoroutineScope): Job = scope.launch {
        while (isActive) {
            val relayed = relayBatch()
                .onFailure { logger.error("Failed to relay outbox events, retrying in {}. Error: {}", idleDelay, it.message) }
                .getOrDefault(0)
            if (relayed < batchSize) delay(idleDelay)
        }
    }

    companion object {
        const val DEFAULT_BATCH_SIZE = 100
        val DEFAULT_IDLE_DELAY = 500.milliseconds
    }
}
