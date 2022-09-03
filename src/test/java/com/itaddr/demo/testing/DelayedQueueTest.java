package com.itaddr.demo.testing;

import com.itaddr.common.tools.beans.Pair;
import com.itaddr.common.tools.utils.ByteUtil;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class DelayedQueueTest {

    private static final AtomicBoolean stopping = new AtomicBoolean(Boolean.FALSE);

    private static final CountDownLatch countDownLatch = new CountDownLatch(NumberUtils.INTEGER_ONE);

    private static final List<TopicPartition> subscriptionList = new ArrayList<>();

    private static final DelayQueue<DelayedPartition> delayQueue = new DelayQueue<>();

    @Test
    public void producer() {
        final KafkaProducer<String, String> kafkaProducer = createKafkaProducer();

        final ProducerRecord<String, String> record = new ProducerRecord<>("delayed-test-10s", 0, System.currentTimeMillis(), "aaaaaaaaaaaaaaa", "bbbbbbbbbbbbbbbb", new ArrayList<>());
        kafkaProducer.send(record);

        kafkaProducer.close();
    }

    public static void main(String[] args) throws InterruptedException {

    }

    private static void consumePauseTest() {
        // 初始化消费者
        final KafkaConsumer<String, String> kafkaConsumer = createKafkaConsumer();
        // 订阅topic
        subscribe(kafkaConsumer, Collections.singletonList("delayed-test-10s"));

        new Thread(() -> {
            final Duration duration = Duration.ofSeconds(1);

            while (!stopping.get()) {
                kafkaConsumer.pause(subscriptionList);

                final ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(duration);
                System.out.println("拉取消息成功");
                if (consumerRecords.isEmpty()) {
                    continue;
                }

                for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                    System.out.println(consumerRecord.value());
                }

                kafkaConsumer.commitAsync();
            }

            countDownLatch.countDown();
        }).start();

        shutdownHook(kafkaConsumer, null);

        new Scanner(System.in).next();
    }

    private static void consumeDelayedTest() {
        // 初始化消费者
        final KafkaConsumer<String, String> kafkaConsumer = createKafkaConsumer();
        // 初始化生产者
        final KafkaProducer<String, String> kafkaProducer = createKafkaProducer();
        // 订阅topic
        subscribe(kafkaConsumer, Collections.singletonList("delayed-test-10s"));

        new Thread(() -> {
            final Duration duration = Duration.ofSeconds(1);

            while (!stopping.get()) {
                final ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(duration);
                System.out.println("拉取消息成功");
                if (consumerRecords.isEmpty()) {
                    continue;
                }

                final Map<TopicPartition, Pair<Long, Long>> delayedPartitionMap = new HashMap<>();
                for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {

                    final TopicPartition topicPartition = new TopicPartition(consumerRecord.topic(), consumerRecord.partition());
                    if (delayedPartitionMap.containsKey(topicPartition)) {
                        continue;
                    }

                    // 获取目标队列相关信息
                    final Pair<String, Long> targetTopic = getTargetTopic(consumerRecord);
                    final String targetTopicName = targetTopic.getLeft();
                    final long consumeTimeMillis = targetTopic.getRight();
                    if (StringUtils.isBlank(targetTopicName)) {
                        continue;
                    }

                    final long offset = consumerRecord.offset();
                    if (consumeTimeMillis > System.currentTimeMillis()) {
                        kafkaConsumer.pause(Collections.singletonList(topicPartition));


                        delayedPartitionMap.put(topicPartition, Pair.of(offset, consumeTimeMillis));
                        continue;
                    }

                    final String key = consumerRecord.key();
                    final String value = consumerRecord.value();

                    final ProducerRecord<String, String> producerRecord = new ProducerRecord<>(targetTopicName, null, null, key, value);
                    kafkaProducer.send(producerRecord);
                }

                long minConsumeTimeMillis = System.currentTimeMillis();
                for (Map.Entry<TopicPartition, Pair<Long, Long>> entry : delayedPartitionMap.entrySet()) {
                    final TopicPartition topicPartition = entry.getKey();
                    final Pair<Long, Long> value = entry.getValue();
                    kafkaConsumer.seek(topicPartition, value.getLeft());
                    minConsumeTimeMillis = Math.min(minConsumeTimeMillis, value.getRight());
                }
                kafkaConsumer.commitAsync();
                if (minConsumeTimeMillis - System.currentTimeMillis() > 0) {
                    kafkaConsumer.pause(delayedPartitionMap.keySet());

                    synchronized (DelayedQueueTest.class) {
                        try {
                            DelayedQueueTest.class.wait(minConsumeTimeMillis - System.currentTimeMillis());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            kafkaConsumer.resume(kafkaConsumer.paused());
                        }
                    }

                }

            }

            countDownLatch.countDown();
        }).start();

        shutdownHook(kafkaConsumer, kafkaProducer);

        new Scanner(System.in).next();
    }

    private static KafkaConsumer<String, String> createKafkaConsumer() {
        final Properties consumerProp = new Properties();
        consumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.0.97:9092,192.168.0.98:9092,192.168.0.99:9092");
        consumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, "local-test-10001");
        consumerProp.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        consumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProp.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        consumerProp.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "10000");
        consumerProp.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "30000");
        consumerProp.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "500");

        return new KafkaConsumer<>(consumerProp);
    }

    private static KafkaProducer<String, String> createKafkaProducer() {
        final Properties producerProp = new Properties();
        producerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.0.97:9092,192.168.0.98:9092,192.168.0.99:9092");
        producerProp.put(ProducerConfig.ACKS_CONFIG, "1");
        producerProp.put(ProducerConfig.RETRIES_CONFIG, "0");
        producerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        producerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        return new KafkaProducer<>(producerProp);
    }

    private static Pair<String, Long> getTargetTopic(ConsumerRecord<?, ?> record) {
        String targetTopicName = null;
        Long consumeTimeMillis = null;
        final Iterator<Header> iterator = record.headers().iterator();
        for (Header header; iterator.hasNext() && null != (header = iterator.next()); ) {
            final String key = header.key();
            final byte[] value = header.value();
            if ("_TARGET_TOPIC_NAME".equals(key)) {
                targetTopicName = new String(value, StandardCharsets.UTF_8);
            } else if ("_CONSUME_TIME_MILLIS".equals(key)) {
                consumeTimeMillis = ByteUtil.readLong(value);
            }
        }
        consumeTimeMillis = null == consumeTimeMillis ? record.timestamp() : consumeTimeMillis;
        return Pair.of(targetTopicName, consumeTimeMillis < 0 ? 0 : consumeTimeMillis);
    }

    private static void subscribe(KafkaConsumer<String, String> kafkaConsumer, List<String> topicNameList) {
        kafkaConsumer.subscribe(topicNameList, new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                subscriptionList.clear();
                final Set<TopicPartition> paused = kafkaConsumer.paused();
                System.out.printf("失去分区: partitions = %s, paused = %s\n", partitions.toString(), paused.toString());
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                subscriptionList.addAll(partitions);
                final Set<TopicPartition> paused = kafkaConsumer.paused();
                System.out.printf("订阅分区: partitions = %s, paused = %s\n", partitions.toString(), paused.toString());
            }
        });
    }

    private static void shutdownHook(KafkaConsumer<String, String> kafkaConsumer, KafkaProducer<String, String> kafkaProducer) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (stopping.compareAndSet(Boolean.FALSE, Boolean.TRUE)) {
                try {
                    System.out.println("消费任务结束，开始关闭消费者和生产者.....");
                    if (!countDownLatch.await(30, TimeUnit.SECONDS)) {
                        System.err.println("等待任务结束超时");
                    }
                    if (null != kafkaConsumer) {
                        kafkaConsumer.close();
                    }
                    if (null != kafkaProducer) {
                        kafkaProducer.close();
                    }
                    System.out.println("成功释放所有资源");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }));
    }

    static class DelayedPartition implements Delayed {

        private final TopicPartition partition;

        private final long offset;

        private final long delayTimeMillis;

        public DelayedPartition(TopicPartition partition, long offset, long delayTimeMillis) {
            this.partition = partition;
            this.offset = offset;
            this.delayTimeMillis = delayTimeMillis;
        }

        @Override
        public long getDelay(@NotNull TimeUnit unit) {
            return this.delayTimeMillis - System.currentTimeMillis();
        }

        @Override
        public int compareTo(@NotNull Delayed o) {
            return Long.compare(delayTimeMillis, ((DelayedPartition) o).delayTimeMillis);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DelayedPartition that = (DelayedPartition) o;
            return Objects.equals(partition, that.partition);
        }

        @Override
        public int hashCode() {
            return Objects.hash(partition);
        }

        @Override
        public String toString() {
            return partition.toString();
        }

    }

}
