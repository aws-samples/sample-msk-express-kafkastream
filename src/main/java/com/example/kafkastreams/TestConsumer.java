package com.example.kafkastreams;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class TestConsumer {
    private static final Logger logger = LoggerFactory.getLogger(TestConsumer.class);

    public static void main(String[] args) {
        String topic = args.length > 0 ? args[0] : "output-topic";
        logger.info("Starting Test Consumer for topic: {}", topic);

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "boot-x0p.sayemexpressnew.ku53xh.c3.kafka.ap-southeast-2.amazonaws.com:9092,boot-wv2.sayemexpressnew.ku53xh.c3.kafka.ap-southeast-2.amazonaws.com:9092,boot-0gc.sayemexpressnew.ku53xh.c3.kafka.ap-southeast-2.amazonaws.com:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Arrays.asList(topic));
            
            logger.info("Consumer subscribed to topic: {}", topic);
            logger.info("Waiting for messages... (Press Ctrl+C to stop)");

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                
                for (ConsumerRecord<String, String> record : records) {
                    logger.info("Received message - Topic: {}, Partition: {}, Offset: {}, Key: {}, Value: {}", 
                        record.topic(), record.partition(), record.offset(), record.key(), record.value());
                }
            }
        } catch (Exception e) {
            logger.error("Error in consumer", e);
        }
    }
} 