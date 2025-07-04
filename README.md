# Kafka Streams MSK Express Example

This is a simple Kafka Streams application designed to work with Amazon MSK Express. The application demonstrates basic stream processing operations while being compatible with MSK Express limitations.

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

- `bootstrap.servers`: Your MSK Express bootstrap servers
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

- **No Segment Configs**: The application avoids using `segment.bytes`, `segment.ms`, and `segment.index.bytes` configurations that are not supported by MSK Express
- **Pre-created Topics**: Internal topics are created programmatically with MSK Express-compatible configurations
- **DeleteRecords API**: Some warnings about unsupported DeleteRecords API may appear but can be safely ignored

## Troubleshooting

### Topic Partition Mismatch
If you see errors about partition count mismatches, delete the existing internal topics and restart the application to recreate them with the correct partition count.

### DeleteRecords Warnings
Warnings about DeleteRecords API being unsupported are normal and can be ignored - they don't affect application functionality.

## Architecture

The application topology includes:
1. **Input Processing**: Parses JSON messages and extracts words
2. **Word Counting**: Aggregates word frequencies using a named state store
3. **Message Filtering**: Filters messages by type using a named operator
4. **Output Generation**: Produces formatted results to output topics

All operators are named to ensure consistent topic naming and state linkage across application restarts and topology modifications.

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

## MSK Express Compatibility

This application is specifically configured to work with MSK Express by:

1. **Avoiding Segment Configurations**: MSK Express doesn't support segment-related topic configurations like:
   - `segment.bytes`
   - `segment.ms`
   - `segment.index.bytes`

2. **Using Compatible Settings**:
   - `replication.factor=1` (MSK Express requirement)
   - `cleanup.policy=delete`
   - `retention.ms=86400000` (24 hours)

3. **Minimal Internal Topic Configuration**: Only essential configurations are applied to internal topics.

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

## Troubleshooting

### Common Issues

1. **Connection Refused**: Check your MSK endpoint and network connectivity
2. **Topic Not Found**: Ensure topics are created before running the application
3. **Permission Denied**: Verify IAM roles and security groups for MSK access

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

## Development

### Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/example/kafkastreams/
│   │       ├── MSKStreamsApp.java      # Main application
│   │       ├── TestProducer.java       # Test message producer
│   │       └── TestConsumer.java       # Test message consumer
│   └── resources/
│       └── application.properties      # Configuration
```

### Customization

To modify the stream processing logic, edit the `buildTopology()` method in `MSKStreamsApp.java`.

## Security

For production use with MSK:

1. **SASL/SCRAM Authentication**: Add SASL configuration
2. **SSL/TLS**: Enable SSL for secure communication
3. **IAM Authentication**: Use IAM roles for authentication

Example SASL configuration:
```properties
kafka.security.protocol=SASL_SSL
kafka.sasl.mechanism=SCRAM-SHA-512
kafka.sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username="your-username" password="your-password";
```

## License

This project is provided as an example and can be used freely for learning and development purposes. 