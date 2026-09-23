package com.bookk.core.data.eventstreaming.impl.embedded

import com.bookk.core.data.eventstreaming.impl.kafka.KeyedTestEvent
import com.bookk.core.data.eventstreaming.registerReceiver
import com.bookk.core.data.eventstreaming.send
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentLinkedQueue

internal class EmbeddedEventStreamingTest {

    private class SutFixture {
        val topicQueueHolder = TopicQueueHolder<String>()
        val producer = EmbeddedEventProducer(topicQueueHolder)
    }

    @Test
    fun `should deliver published event to its subscriber`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val topic = "topic.single"
        val consumer = EmbeddedEventConsumer(fixture.topicQueueHolder)
        val received = ConcurrentLinkedQueue<KeyedTestEvent>()
        consumer.registerReceiver<KeyedTestEvent, String>(topic) { event -> received += event }
        consumer.start(backgroundScope)

        whenn()
        fixture.producer.send(KeyedTestEvent(entityId = "entity-1", sequence = 0, topic = topic))
        advanceUntilIdle()

        then()
        assertEquals(1, received.size)
    }

    @Test
    fun `should fan out published event to every subscriber of the same topic`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val topic = "topic.shared"
        val firstServiceConsumer = EmbeddedEventConsumer(fixture.topicQueueHolder)
        val secondServiceConsumer = EmbeddedEventConsumer(fixture.topicQueueHolder)
        val firstReceived = ConcurrentLinkedQueue<KeyedTestEvent>()
        val secondReceived = ConcurrentLinkedQueue<KeyedTestEvent>()
        firstServiceConsumer.registerReceiver<KeyedTestEvent, String>(topic) { event -> firstReceived += event }
        secondServiceConsumer.registerReceiver<KeyedTestEvent, String>(topic) { event -> secondReceived += event }
        firstServiceConsumer.start(backgroundScope)
        secondServiceConsumer.start(backgroundScope)

        whenn()
        fixture.producer.send(KeyedTestEvent(entityId = "entity-1", sequence = 0, topic = topic))
        advanceUntilIdle()

        then()
        assertEquals(1, firstReceived.size)
        assertEquals(1, secondReceived.size)
    }

    @Test
    fun `should not deliver event to a consumer subscribed to a different topic`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val consumer = EmbeddedEventConsumer(fixture.topicQueueHolder)
        val received = ConcurrentLinkedQueue<KeyedTestEvent>()
        consumer.registerReceiver<KeyedTestEvent, String>("topic.interested") { event -> received += event }
        consumer.start(backgroundScope)

        whenn()
        fixture.producer.send(KeyedTestEvent(entityId = "entity-1", sequence = 0, topic = "topic.other"))
        advanceUntilIdle()

        then()
        assertTrue(received.isEmpty())
    }
}
