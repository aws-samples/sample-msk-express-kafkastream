#!/bin/bash

# Kafka Streams MSK Example - Run Script

echo "=== Kafka Streams MSK Example ==="
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 11 ]; then
    echo "Error: Java 11 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

echo "Java version: $(java -version 2>&1 | head -n 1)"
echo "Maven version: $(mvn -version 2>&1 | head -n 1)"
echo ""

# Create state directory
echo "Creating state directory..."
mkdir -p /tmp/kafka-streams-state
chmod 755 /tmp/kafka-streams-state

# Build the project
echo "Building the project..."
mvn clean package

if [ $? -ne 0 ]; then
    echo "Error: Build failed"
    exit 1
fi

echo ""
echo "Build successful!"
echo ""

# Check if JAR file exists
if [ ! -f "target/kafka-streams-msk-1.0.0.jar" ]; then
    echo "Error: JAR file not found. Build may have failed."
    exit 1
fi

echo "=== Available Commands ==="
echo "1. Run Streams Application:"
echo "   java -jar target/kafka-streams-msk-1.0.0.jar"
echo ""
echo "2. Send Test Messages:"
echo "   mvn exec:java -Dexec.mainClass=\"com.example.kafkastreams.TestProducer\""
echo ""
echo "3. View Word Count Results:"
echo "   mvn exec:java -Dexec.mainClass=\"com.example.kafkastreams.TestConsumer\" -Dexec.args=\"output-topic\""
echo ""
echo "4. View Important Messages:"
echo "   mvn exec:java -Dexec.mainClass=\"com.example.kafkastreams.TestConsumer\" -Dexec.args=\"important-messages-topic\""
echo ""

# Ask user what to do
echo "What would you like to do?"
echo "1) Run the Streams Application"
echo "2) Send Test Messages"
echo "3) View Word Count Results"
echo "4) View Important Messages"
echo "5) Exit"
echo ""
read -p "Enter your choice (1-5): " choice

case $choice in
    1)
        echo "Starting Kafka Streams Application..."
        java -jar target/kafka-streams-msk-1.0.0.jar
        ;;
    2)
        echo "Sending test messages..."
        mvn exec:java -Dexec.mainClass="com.example.kafkastreams.TestProducer"
        ;;
    3)
        echo "Starting consumer for word count results..."
        mvn exec:java -Dexec.mainClass="com.example.kafkastreams.TestConsumer" -Dexec.args="output-topic"
        ;;
    4)
        echo "Starting consumer for important messages..."
        mvn exec:java -Dexec.mainClass="com.example.kafkastreams.TestConsumer" -Dexec.args="important-messages-topic"
        ;;
    5)
        echo "Exiting..."
        exit 0
        ;;
    *)
        echo "Invalid choice. Exiting..."
        exit 1
        ;;
esac 