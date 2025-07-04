package com.example.kafkastreams;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class CreateInternalTopics {
    public static void createTopics(String bootstrapServers, List<String> topics, String inputTopic) throws Exception {
        Properties adminProps = new Properties();
        adminProps.put("bootstrap.servers", bootstrapServers);
        try (AdminClient admin = AdminClient.create(adminProps)) {
            // Get partition count of input topic
            int partitionCount = 1;
            try {
                DescribeTopicsResult describeTopicsResult = admin.describeTopics(Collections.singletonList(inputTopic));
                Map<String, TopicDescription> descriptions = describeTopicsResult.allTopicNames().get();
                if (descriptions.containsKey(inputTopic)) {
                    partitionCount = descriptions.get(inputTopic).partitions().size();
                }
            } catch (Exception e) {
                System.out.println("Could not determine partition count for input topic. Defaulting to 1 partition.");
            }

            List<NewTopic> newTopics = new ArrayList<>();
            for (String topic : topics) {
                NewTopic newTopic = new NewTopic(topic, partitionCount, (short) 1)
                        .configs(Map.of(
                                "cleanup.policy", "delete",
                                "retention.ms", "86400000"
                        ));
                newTopics.add(newTopic);
            }
            try {
                Map<String, KafkaFuture<Void>> results = admin.createTopics(newTopics).values();
                for (String topic : results.keySet()) {
                    try {
                        results.get(topic).get();
                        System.out.println("Created topic: " + topic);
                    } catch (Exception e) {
                        if (e.getCause() != null && e.getCause().getClass().getSimpleName().equals("TopicExistsException")) {
                            System.out.println("Topic already exists: " + topic);
                        } else {
                            throw e;
                        }
                    }
                }
            } catch (Exception e) {
                throw e;
            }
        }
    }
} 