import java.util.Random;
import java.util.logging.Logger;

public class Consumer implements Runnable {
    private final String name;
    private final MessageQueue queue;
    private final Random random = new Random();
    private static final Logger logger = Logger.getLogger(Consumer.class.getName());

    public Consumer(String name, MessageQueue queue) {
        this.name = name;
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String message = queue.dequeue();
                try {
                    int random_number = random.nextInt(10);
                    // 20% chance of success
                    if (random_number > 2) {
                        // Simulate processing time to complete the task
                        Thread.sleep(random.nextInt(500));
                        logger.info("Consumed successfully: " + message + " by " + this.name);
                        queue.acknowledge(message);
                    } else {
                        // Simulate some time before error
                        Thread.sleep(random.nextInt(100));
                        logger.warning("Error happened while processing: " + message + " by " + this.name);
                        throw new RuntimeException("Processing failed");
                    }
                } catch (Exception e) {
                    // consumer took some time (2 seconds) to restart or become available
                    Thread.sleep(2000);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.severe("Consumer interrupted: " + name);
        }
    }
}
