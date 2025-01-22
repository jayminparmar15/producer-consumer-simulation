import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class MessageQueueAppTest {
    private MessageQueue queue;
    private Producer producer;
    private Consumer consumer;

    @BeforeEach
    void setUp() {
        queue = new MessageQueue(1000);
    }

    @AfterEach
    void tearDown() {
        queue.shutdownNow();
    }

    @Test
    void testMessageEnqueueAndDequeue() throws InterruptedException {
        String testMessage = "Test Message";
        queue.enqueue(testMessage);
        String received = queue.dequeue();
        assertEquals(testMessage, received);
    }

    @Test
    void testVisibilityTimeout() throws InterruptedException {
        String testMessage = "Timeout Test";
        queue.enqueue(testMessage);

        // Dequeue but don't acknowledge
        String received = queue.dequeue();
        assertEquals(testMessage, received);

        // Wait for visibility timeout which we set 1 second
        Thread.sleep(1500);

        // Message should be back in queue
        assertEquals(1, queue.getCurrentQueueSize());
        String receivedAgain = queue.dequeue();
        assertEquals(testMessage, receivedAgain);
        assertEquals(1, queue.getErrorCount());
    }

    @Test
    void testMessageAcknowledgment() throws InterruptedException {
        String testMessage = "Ack Test";
        queue.enqueue(testMessage);

        String received = queue.dequeue();
        queue.acknowledge(received);

        // Wait to ensure message isn't requeued
        Thread.sleep(1500);
        assertEquals(0, queue.getCurrentQueueSize());
        assertEquals(1, queue.getSuccessCount());
        assertEquals(0, queue.getErrorCount());
    }

    @Test
    void testProducerOperation() throws InterruptedException {
        Producer producer = new Producer("TestProducer", queue, 3);
        Thread producerThread = new Thread(producer);
        producerThread.start();
        producerThread.join();

        // Verify all messages were produced
        int messageCount = 0;
        for (int i = 0; i < 3; i++) {
            String message = queue.dequeue();
            assertNotNull(message);
            assertEquals(message, "TestProducer" + "'s Task-" + i);
            messageCount++;
        }
        assertEquals(3, messageCount);
    }

    @Test
    void testConsumerOperation() throws InterruptedException {
        // Enqueue test messages
        queue.enqueue("Test1");
        queue.enqueue("Test2");

        Consumer consumer = new Consumer("TestConsumer", queue);
        Thread consumerThread = new Thread(consumer);
        consumerThread.start();

        // Give consumer time to process
        Thread.sleep(3000);
        consumerThread.interrupt();

        // Verify some messages were processed. either message are processed or error occurred.
        assertTrue(queue.getSuccessCount() + queue.getErrorCount() > 0);
    }

    @Test
    void testConcurrentOperations() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(5);

        // Start 2 producers
        executor.execute(new Producer("Producer-1", queue, 3));

        executor.execute(new Producer("Producer-2", queue, 3));


        // Start 3 consumers
        executor.execute(new Consumer("Consumer-1", queue));
        executor.execute(new Consumer("Consumer-2", queue));
        executor.execute(new Consumer("Consumer-3", queue));


        // Allow time for message processing
        Thread.sleep(10000);

        executor.shutdownNow();

        // Verify total processed messages success equals produced messages (6)
        assertEquals(6, queue.getSuccessCount());
    }
}