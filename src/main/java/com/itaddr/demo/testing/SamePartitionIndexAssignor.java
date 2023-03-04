package com.itaddr.demo.testing;

import org.apache.kafka.clients.consumer.internals.AbstractPartitionAssignor;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.utils.CircularIterator;
import org.apache.kafka.common.utils.Utils;

import java.util.*;

/**
 * @ClassName SamePartitionIndexAssignor
 * @Description properties.put(" partition.assignment.strategy ", MyCustomerPartitioner.class);
 * @Author MyPC
 * @Date 2022/10/12 19:53
 * @Version 1.0
 */
public class SamePartitionIndexAssignor extends AbstractPartitionAssignor {

    @Override
    public List<RebalanceProtocol> supportedProtocols() {
        return Arrays.asList(RebalanceProtocol.COOPERATIVE, RebalanceProtocol.EAGER);
    }

    /**
     * @param partitionsPerTopic 所订阅的每个 topic 与其 partition 数的对应关系
     * @param subscriptions      每个 consumerId 与其所订阅的 topic 列表的关系。
     * @return
     */
    @Override
    public Map<String, List<TopicPartition>> assign(Map<String, Integer> partitionsPerTopic, Map<String, Subscription> subscriptions) {
        Map<String, List<TopicPartition>> assignment = new HashMap<>();
        for (String memberId : subscriptions.keySet()) {
            assignment.put(memberId, new ArrayList<>());
        }

        CircularIterator<String> assigner = new CircularIterator<>(Utils.sorted(subscriptions.keySet()));
        for (TopicPartition partition : allPartitionsSorted(partitionsPerTopic, subscriptions)) {
            final String topic = partition.topic();
            while (!subscriptions.get(assigner.peek()).topics().contains(topic)) {
                assigner.next();
            }
            assignment.get(assigner.next()).add(partition);
        }
        return assignment;
    }

    public List<TopicPartition> allPartitionsSorted(Map<String, Integer> partitionsPerTopic, Map<String, Subscription> subscriptions) {
        SortedSet<String> topics = new TreeSet<>();
        for (Subscription subscription : subscriptions.values()) {
            topics.addAll(subscription.topics());
        }

        List<TopicPartition> allPartitions = new ArrayList<>();
        for (String topic : topics) {
            Integer numPartitionsForTopic = partitionsPerTopic.get(topic);
            if (numPartitionsForTopic != null) {
                allPartitions.addAll(AbstractPartitionAssignor.partitions(topic, numPartitionsForTopic));
            }
        }
        return allPartitions;
    }

    @Override
    public String name() {
        return "same-partition-index";
    }

}
