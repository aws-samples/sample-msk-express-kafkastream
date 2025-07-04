# Kafka Streams MSK Express Example

This is a sample Kafka Streams application designed to work with Amazon MSK Express rokers. The application demonstrates basic stream processing operations while being compatible with MSK Express brokers.

## Features

- **MSK Express Compatible**: Avoids segment-related configurations that are not supported by MSK Express
- **Named Operators**: Uses named operators for consistent state store and topic naming across application restarts
- **Pre-created Internal Topics**: Automatically creates internal topics with correct configurations
- **Word Count Example**: Demonstrates basic stream processing with JSON message parsing
- **Message Filtering**: Shows how to filter messages based on message type

## Named Operators Benefits

This application uses named operators throughout the topology to ensure consistent state store and topic naming:

- **Consistent Topic Names**: Instead of auto-generated names like `msk-streams-app-KSTREAM-AGGREGATE-STATE-STORE-0000000004-repartition`, we use predictable names like `msk-streams-app-word-count-store-changelog`
- **State Linkage Preservation**: Named operators maintain their state linkage even when the topology is modified (adding/removing/reordering operators)
- **Easier Debugging**: Predictable topic names make it easier to monitor and debug the application
- **Production Stability**: Prevents state loss during application upgrades or topology changes

### Named Operators Used:
- `parse-words`: Parses JSON messages and extracts words
- `word-to-count`: Maps words to count values
- `group-by-word`: Groups words for aggregation
- `word-count-store`: State store for word counts
- `format-output`: Formats output messages
- `filter-important-messages`: Filters messages by type

## Prerequisites

- Java 11 or higher
- Maven (or use the included Maven wrapper)
- Access to Amazon MSK Express cluster
- Input topic `input-topic` must exist with the correct partition count

## Input Message Format

The application expects JSON messages in the following format:

```json
{
  "id": "123",
  "content": "Hello world example",
  "type": "important",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## Output Topics

- `output-topic`: Contains word count results in JSON format
- `important-messages-topic`: Contains only messages with type "important"

## Building and Running

### Using Maven Wrapper (Recommended)

**Windows:**
```bash
.\mvnw.cmd clean package
java -jar target/kafka-streams-msk-1.0.0.jar
```

**Linux/Mac:**
```bash
./mvnw clean package
java -jar target/kafka-streams-msk-1.0.0.jar
```

### Using System Maven

```bash
mvn clean package
java -jar target/kafka-streams-msk-1.0.0.jar
```

## Configuration

Update the following in `src/main/resources/application.properties`:

- `bootstrap.servers`: Your MSK Express rokers bootstrap url
- `application.id`: Unique application identifier
- Other Kafka Streams configurations as needed

## Testing

### Producer Test
```bash
java -cp target/kafka-streams-msk-1.0.0.jar com.example.kafkastreams.TestProducer
```

### Consumer Test
```bash
java -cp target/kafka-streams-msk-1.0.0.jar com.example.kafkastreams.TestConsumer
```

## MSK Express Compatibility Notes

- **No Segment Configs**: The application avoids using `segment.bytes`, `segment.ms`, and `segment.index.bytes` configurations that are not supported in MSK Express brokers
- **Pre-created Topics**: Internal topics are created programmatically with MSK Express-compatible configurations


## Troubleshooting

### Topic Partition Mismatch
If you see errors about partition count mismatches, delete the existing internal topics and restart the application to recreate them with the correct partition count.

## Message Format

The application expects JSON messages in this format:

```json
{
  "id": "message-id",
  "content": "message content for word counting",
  "type": "normal|important",
  "timestamp": 1234567890
}
```



## Application Topology

The streams application performs these operations:

1. **Input Processing**: Reads from `input-topic`
2. **Word Counting**: 
   - Parses JSON messages
   - Splits content into words
   - Counts word occurrences
   - Outputs to `output-topic`
3. **Message Filtering**:
   - Filters messages by type
   - Routes important messages to `important-messages-topic`

### Logs

The application uses SLF4J with Simple logger. Log levels can be configured in `application.properties`:

```properties
logging.level.com.example.kafkastreams=INFO
logging.level.org.apache.kafka=WARN
```

### State Directory

The application stores state in `/tmp/kafka-streams-state`. Ensure this directory is writable:

```bash
mkdir -p /tmp/kafka-streams-state
chmod 755 /tmp/kafka-streams-state
```



