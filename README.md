# producer-consumer-simulation

A Java implementation of a message queue system with multiple producers and consumers.

## Features

- Message queue with visibility timeout
- Multiple producers and consumers running concurrently
- Error handling and message reprocessing
- Message acknowledgment system
- Success/failure tracking

## Classes

```
ApplicationMain.java     # Main application entry point
Consumer.java           # Consumer implementation
MessageQueue.java       # Queue implementation
Producer.java          # Producer implementation

MessageQueueAppTest.java  # Unit tests
```

## Requirements

- Java 8+
- Maven

## Building

```bash
mvn clean install
```

## Running

```bash
mvn exec:java -Dexec.mainClass="ApplicationMain"
```

## Testing

```bash
mvn test
```

## Configuration

The application is configured with:
- 2 producers (3 messages each)
- 3 consumers
- 1-second visibility timeout
- 10-second runtime

Modify these parameters in `ApplicationMain.java` as needed.