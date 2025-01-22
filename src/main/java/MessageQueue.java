import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class MessageQueue {
    // Thread-safe queue for storing messages by producers
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    // Thread-safe HashMap that stores information about expire of task given to consumer
    private final ConcurrentHashMap<String, Long> hiddenMessages = new ConcurrentHashMap<>();
    private final long visibilityTimeout;
    // Backgrounf process thread
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private static final Logger logger = Logger.getLogger(MessageQueue.class.getName());

    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger errorCount = new AtomicInteger(0);

    public MessageQueue(long visibilityTimeoutMillis) {
        this.visibilityTimeout = visibilityTimeoutMillis;

        // Start a background thread to monitor hidden messages
        scheduler.scheduleAtFixedRate(this::monitorHiddenMessages, 0, 500, TimeUnit.MILLISECONDS);
    }

    public void enqueue(String message) {
        queue.offer(message);
        logger.info("Message enqueued: " + message);
    }

    public String dequeue() throws InterruptedException {
        while (true) {
            String message = queue.poll(1, TimeUnit.SECONDS);
            if (message == null) {
                continue; // Wait for a message to be available
            }

            Long expiry = System.currentTimeMillis() + visibilityTimeout;
            Long previous = hiddenMessages.putIfAbsent(message, expiry);
            if (previous == null) {
                logger.info("Message dequeued: " + message);
                return message;
            }
        }
    }

    public void acknowledge(String message) {
        // Remove the message from the hiddenMessages
        hiddenMessages.remove(message);
        successCount.incrementAndGet();
        logger.info("Message acknowledged: " + message);
    }

    public void recordError(String message) {
        errorCount.incrementAndGet();
        logger.warning("Message processing failed: " + message);
    }

    public int getSuccessCount() {
        return successCount.get();
    }

    public int getErrorCount() {
        return errorCount.get();
    }

    public int getCurrentQueueSize() {
        return queue.size();
    }

    private void monitorHiddenMessages() {
        long currentTime = System.currentTimeMillis();
        hiddenMessages.forEach((message, hiddenUntil) -> {
            if (currentTime > hiddenUntil) {
                hiddenMessages.computeIfPresent(message, (k, v) -> {
                    queue.offer(message);
                    recordError(message);
                    logger.warning("Message re-released to queue: " + message);
                    return null;
                });
            }
        });
    }

    public void shutdownNow() {
        scheduler.shutdownNow();
        logger.info("MessageQueue background monitor shutdown.");
    }
}
