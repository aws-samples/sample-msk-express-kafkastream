# Quick Start Guide

## Prerequisites
- Java 11+ installed
- Maven 3.6+ installed
- Access to MSK cluster (or local Kafka)

## 1. Configure MSK Connection

Edit `msk-config.properties` and update the bootstrap servers:

```properties
bootstrap.servers=your-msk-endpoint:9092
```

For MSK Express, the endpoint format is:
```
your-cluster-name.kafka.us-east-1.amazonaws.com:9092
```

## 2. Create Topics

Create these topics in your MSK cluster:
- `input-topic`
- `output-topic` 
- `important-messages-topic`
export bs="boot-x0p.sayemexpressnew.ku53xh.c3.kafka.ap-southeast-2.amazonaws.com:9092,boot-wv2.sayemexpressnew.ku53xh.c3.kafka.ap-southeast-2.amazonaws.com:9092,boot-0gc.sayemexpressnew.ku53xh.c3.kafka.ap-southeast-2.amazonaws.com:9092"
./bin/kafka-topics.sh --bootstrap-server $bs --create --topic input-topic --partitions 9
./bin/kafka-topics.sh --bootstrap-server $bs --create --topic output-topic --partitions 9
./bin/kafka-topics.sh --bootstrap-server $bs --create --topic important-messages-topic --partitions 9

## 3. Build and Run

### Option A: Using the provided scripts

**Linux/Mac:**
```bash
./run.sh
```

**Windows:**
```cmd
run.bat
```

### Option B: Manual commands

```bash
# Build the project
mvn clean package

# Run the streams application
java -jar target/kafka-streams-msk-1.0.0.jar

# In another terminal, send test messages
mvn exec:java -Dexec.mainClass="com.example.kafkastreams.TestProducer"

# In another terminal, view results
mvn exec:java -Dexec.mainClass="com.example.kafkastreams.TestConsumer" -Dexec.args="output-topic"
```

## 4. Expected Output

The application will:
1. Process JSON messages from `input-topic`
2. Count words in message content
3. Filter important messages
4. Output results to `output-topic` and `important-messages-topic`

## 5. Test Message Format

The application expects JSON messages like:
```json
{
  "id": "1",
  "content": "Hello world this is a test",
  "type": "normal",
  "timestamp": 1234567890
}
```

## Troubleshooting

- **Connection issues**: Check your MSK endpoint and network connectivity
- **Topic not found**: Ensure topics are created before running
- **Permission denied**: Verify IAM roles and security groups

## Next Steps

- Customize the stream processing logic in `MSKStreamsApp.java`
- Add security configurations for production use
- Scale the application by adjusting `num.stream.threads` 