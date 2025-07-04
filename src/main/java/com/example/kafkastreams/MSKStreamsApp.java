package com.example.kafkastreams;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Grouped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class MSKStreamsApp {
    private static final Logger logger = LoggerFactory.getLogger(MSKStreamsApp.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        logger.info("Starting Kafka Streams MSK Application...");

        // Pre-create required internal topics for MSK Express
        try {
            List<String> internalTopics = Arrays.asList(
                "msk-streams-app-parse-words-repartition",
                "msk-streams-app-word-to-count-repartition", 
                "msk-streams-app-group-by-word-repartition",
                "msk-streams-app-word-count-store-changelog",
                "msk-streams-app-format-output-repartition",
                "msk-streams-app-filter-important-messages-repartition"
            );
            String bootstrapServers = "boot-x0p.sayemexpressnew.ku53xh.c3.kafka.ap-southeast-2.amazonaws.com:9092,boot-wv2.sayemexpressnew.ku53xh.c3.kafka.ap-southeast-2.amazonaws.com:9092,boot-0gc.sayemexpressnew.ku53xh.c3.kafka.ap-southeast-2.amazonaws.com:9092";
            String inputTopic = "input-topic";
            CreateInternalTopics.createTopics(bootstrapServers, internalTopics, inputTopic);
        } catch (Exception e) {
            logger.error("Failed to pre-create internal topics", e);
            System.exit(1);
        }

        // Configuration
        Properties props = getStreamsConfig();
        
        // Build topology
        StreamsBuilder builder = new StreamsBuilder();
        buildTopology(builder);
        
        // Create and start streams
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        
        // Add shutdown hook
        final CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                logger.info("Shutting down streams application...");
                streams.close(Duration.ofSeconds(5));
                latch.countDown();
            }
        });

        try {
            streams.start();
            logger.info("Streams application started successfully!");
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Application interrupted", e);
            System.exit(1);
        } catch (Exception e) {
            logger.error("Error starting streams application", e);
            System.exit(1);
        }
    }

    private static Properties getStreamsConfig() {
        Properties props = new Properties();
        
        // Basic configuration
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "msk-streams-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "boot-x0p.sayemexpressnew.ku53xh.c3.kafka.ap-southeast-2.amazonaws.com:9092,boot-wv2.sayemexpressnew.ku53xh.c3.kafka.ap-southeast-2.amazonaws.com:9092,boot-0gc.sayemexpressnew.ku53xh.c3.kafka.ap-southeast-2.amazonaws.com:9092");
        
        // Serialization
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        
        // Consumer configuration
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        // Producer configuration
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        
        // State store configuration
        props.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams-state");
        
        // Internal topic configuration - MSK Express compatible
        // Avoid segment-related configs that MSK Express doesn't support
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 1);
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 1);
        
        // Cleanup policy for internal topics
        props.put(StreamsConfig.TOPIC_PREFIX + "cleanup.policy", "delete");
        props.put(StreamsConfig.TOPIC_PREFIX + "retention.ms", "86400000"); // 24 hours
        
        return props;
    }

    private static void buildTopology(StreamsBuilder builder) {
        // Input topic
        KStream<String, String> inputStream = builder.stream("input-topic");
        
        // Print input messages
        inputStream.print(Printed.<String, String>toSysOut().withLabel("INPUT"));
        
        // Simple word count example with named operators
        KStream<String, String> wordCountStream = inputStream
            .flatMapValues(value -> {
                try {
                    // Parse JSON message
                    Message message = objectMapper.readValue(value, Message.class);
                    return java.util.Arrays.asList(message.getContent().toLowerCase().split("\\s+"));
                } catch (Exception e) {
                    logger.warn("Failed to parse message: {}", value, e);
                    return java.util.Arrays.asList("error");
                }
            }, Named.as("parse-words"))
            .map((key, word) -> new org.apache.kafka.streams.KeyValue<>(word, "1"), Named.as("word-to-count"))
            .groupByKey(Grouped.as("group-by-word"))
            .count(Named.as("word-count-store")) // Named state store for consistent topic naming
            .toStream()
            .map((word, count) -> new org.apache.kafka.streams.KeyValue<>(word, 
                String.format("{\"word\":\"%s\",\"count\":%d}", word, count)), Named.as("format-output"));
        
        // Output to result topic
        wordCountStream.to("output-topic");
        
        // Print results
        wordCountStream.print(Printed.<String, String>toSysOut().withLabel("WORD_COUNT"));
        
        // Another example: Filter messages by type with named operator
        KStream<String, String> filteredStream = inputStream
            .filter((key, value) -> {
                try {
                    Message message = objectMapper.readValue(value, Message.class);
                    return "important".equals(message.getType());
                } catch (Exception e) {
                    return false;
                }
            }, Named.as("filter-important-messages"));
        
        filteredStream.to("important-messages-topic");
        filteredStream.print(Printed.<String, String>toSysOut().withLabel("IMPORTANT"));
    }

    // Simple message class for JSON parsing
    public static class Message {
        private String id;
        private String content;
        private String type;
        private long timestamp;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
} 