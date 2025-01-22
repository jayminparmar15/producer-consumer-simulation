
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class ApplicationMain {

    // logger
    private static final Logger logger = Logger.getLogger(ApplicationMain.class.getName());

    public static void main(String[] args) {
        // message queue with 1-second visibility timeout
        MessageQueue queue = new MessageQueue(1000);

        // executorService to run threads. Here, producers and consumers.
        ExecutorService executor = Executors.newFixedThreadPool(5);

        // 2 producers each producing 3 messages in queue
        executor.execute(new Producer("Producer-1", queue, 3));
        executor.execute(new Producer("Producer-2", queue, 3));

        // 3 consumers
        executor.execute(new Consumer("Consumer-1", queue));
        executor.execute(new Consumer("Consumer-2", queue));
        executor.execute(new Consumer("Consumer-3", queue));

        // Allow the system to run for some time, then shut it down
        try {
            Thread.sleep(10000); // Run for 10 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // shutting all threads down.
        executor.shutdownNow();
        queue.shutdownNow();
        logger.info("Application shutting down...");

        // this will be always total message we added which is 6.
        logger.info("Total successful messages: " + queue.getSuccessCount());

        // number of time error occurred.
        logger.info("Total failed messages: " + queue.getErrorCount());
    }
}

