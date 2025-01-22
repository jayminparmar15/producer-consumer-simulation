import java.util.Random;
import java.util.logging.Logger;

public class Producer implements Runnable {
    private final String name;
    private final int messageCount;
    private final MessageQueue queue;
    private final Random random = new Random();

    // logger
    private static final Logger logger = Logger.getLogger(Producer.class.getName());

    public Producer(String name, MessageQueue queue, int messageCount) {
        this.name = name;
        this.queue = queue;
        this.messageCount = messageCount;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < messageCount; i++) {
                String message = this.name + "'s Task-" + i;
                queue.enqueue(message);
                logger.info("Produced successfully: " + message);
                Thread.sleep(random.nextInt(500));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.severe("Producer interrupted: " + name);
        }
    }
}
