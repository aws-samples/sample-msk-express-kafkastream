package com.example.kafkastreams;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class TestProducer {
    private static final Logger logger = LoggerFactory.getLogger(TestProducer.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        logger.info("Starting Test Producer...");

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "boot-x0p.test.ku53xh.c3.kafka.ap-southeast-2.amazonaws.com:9092,boot-wv2.test.ku53xh.c3.kafka.ap-southeast-2.amazonaws.com:9092,boot-0gc.test.ku53xh.c3.kafka.ap-southeast-2.amazonaws.com:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            
            // Sample messages
            String[] messages = {
                createMessage("1", "Hello world this is a test message", "normal"),
                createMessage("2", "This is an important message that should be filtered", "important"),
                createMessage("3", "Another normal message for testing", "normal"),
                createMessage("4", "Critical information that needs attention", "important"),
                createMessage("5", "Regular update message", "normal"),
                createMessage("6", "Urgent notification for immediate action", "important")
            };

            for (String message : messages) {
                ProducerRecord<String, String> record = 
                    new ProducerRecord<>("input-topic", message);
                
                producer.send(record, (metadata, exception) -> {
                    if (exception != null) {
                        logger.error("Error sending message", exception);
                    } else {
                        logger.info("Message sent to topic: {}, partition: {}, offset: {}", 
                            metadata.topic(), metadata.partition(), metadata.offset());
                    }
                });
                
                // Small delay between messages
                TimeUnit.MILLISECONDS.sleep(500);
            }
            
            // Wait for all messages to be sent
            producer.flush();
            logger.info("All test messages sent successfully!");
            
        } catch (Exception e) {
            logger.error("Error in producer", e);
        }
    }

    private static String createMessage(String id, String content, String type) {
        try {
            MSKStreamsApp.Message message = new MSKStreamsApp.Message();
            message.setId(id);
            message.setContent(content);
            message.setType(type);
            message.setTimestamp(System.currentTimeMillis());
            
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            logger.error("Error creating message", e);
            return "{\"id\":\"" + id + "\",\"content\":\"" + content + "\",\"type\":\"" + type + "\"}";
        }
    }
} 