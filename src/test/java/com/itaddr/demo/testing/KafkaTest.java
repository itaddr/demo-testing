package com.itaddr.demo.testing;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

/**
 * @Title null.java
 * @Package com.itaddr.demo.testing
 * @Author 马嘉祺
 * @Date 2021/11/5 16:36
 * @Description
 */
public class KafkaTest {

    private KafkaConsumer<String, String> consumer;

    @Before
    public void before() {
        Properties prop = new Properties();
        prop.setProperty("bootstrap.servers", "192.168.0.97:9092,192.168.0.98:9092,192.168.0.99:9092");
        prop.setProperty("group.id", "3367-activity-role-consumer");
        prop.setProperty("auto.offset.reset", "latest");
        prop.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        prop.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        prop.setProperty("enable.auto.commit", "false");
        prop.setProperty("auto.commit.interval", "10000");
        prop.setProperty("fetch.max.wait", "30000");
        consumer = new KafkaConsumer<>(prop);
    }

    @After
    public void close() {
        consumer.close();
    }

    @Test
    public void test01() {

    }

}
