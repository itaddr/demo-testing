package com.itaddr.demo.testing;

import com.alibaba.fastjson.JSON;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.config.ConfigResource;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * @Title null.java
 * @Package com.itaddr.demo.testing
 * @Author 马嘉祺
 * @Date 2021/11/5 16:36
 * @Description
 */
public class KafkaTest {

    @Test
    public void test01() {
        System.out.println(((int) '0') - 48);
        System.out.println(Math.pow(10, 0));
    }

    private AdminClient createAdminClient() {
        final Properties properties = new Properties();
        properties.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.0.97:9092,192.168.0.98:9092,192.168.0.99:9092");
        properties.setProperty(AdminClientConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, "10000");
        properties.setProperty(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "30000");
        return KafkaAdminClient.create(properties);
    }

    @Test
    public void listTopicsTest() throws ExecutionException, InterruptedException {
        final String dynamicDelayTopic = "__delayed_message_anytime", delayTopicPrefix = "__delayed_message_";
        final String secStr = "sec", minStr = "min", hourStr = "hour", dayStr = "day";

        final AdminClient adminClient = this.createAdminClient();

        final ListTopicsOptions listTopicsOptions = new ListTopicsOptions();
        listTopicsOptions.listInternal(true);
        final ListTopicsResult listTopicsResult = adminClient.listTopics(listTopicsOptions);
        for (Map.Entry<String, TopicListing> entry : listTopicsResult.namesToListings().get().entrySet()) {
            final String topicName = entry.getKey();
            if (dynamicDelayTopic.equals(topicName)) {
                System.out.println(topicName + ": anytime");
            } else if (delayTopicPrefix.length() + 4 <= topicName.length()) {
                if (!topicName.startsWith(delayTopicPrefix)) {
                    continue;
                }

                final int init, unit;
                if (topicName.endsWith(secStr)) {
                    init = topicName.length() - 4;
                    unit = 1000;
                } else if (topicName.endsWith(minStr)) {
                    init = topicName.length() - 4;
                    unit = 60 * 1000;
                } else if (delayTopicPrefix.length() + 4 < topicName.length() && topicName.endsWith(hourStr)) {
                    init = topicName.length() - 5;
                    unit = 60 * 60 * 1000;
                } else if (topicName.endsWith(dayStr)) {
                    init = topicName.length() - 4;
                    unit = 24 * 60 * 60 * 1000;
                } else {
                    continue;
                }
                long staticTime = 0;
                for (int i = init, num = 0; i >= delayTopicPrefix.length(); --i, ++num) {
                    char c = topicName.charAt(i);
                    if (c >= '0' && c <= '9') {
                        staticTime += ((c & 0xffff) - 48) * Math.pow(10, num) * unit;
                    } else {
                        staticTime = -1;
                        break;
                    }
                }
                System.out.println(topicName + ": " + staticTime + "ms");
            }
        }

        adminClient.close();
    }

    @Test
    public void describeConfigsTest() throws ExecutionException, InterruptedException {
        final AdminClient adminClient = this.createAdminClient();

        final DescribeConfigsResult describeConfigsResult = adminClient.describeConfigs(Collections.singletonList(new ConfigResource(ConfigResource.Type.TOPIC, "__delayed_message_30sec")));
        for (Map.Entry<ConfigResource, Config> entry : describeConfigsResult.all().get().entrySet()) {
            final ConfigResource configResource = entry.getKey();
            final Config config = entry.getValue();
            System.out.printf("name=%s, type=%s, isDefault=%b\n", configResource.name(), configResource.type().name(), configResource.isDefault());
            for (ConfigEntry configEntry : config.entries()) {
                System.out.printf("\tname=%s, value=%s, isDefault=%b, isReadOnly=%b, isSensitive=%b, source=%s, synonyms=%s\n",
                        configEntry.name(), configEntry.value(), configEntry.isDefault(), configEntry.isReadOnly(), configEntry.isSensitive(), configEntry.source().name(), JSON.toJSONString(configEntry.synonyms()));
            }
        }

        adminClient.close();
    }

    @Test
    public void incrementalAlterConfigsTest() throws ExecutionException, InterruptedException {
        final AdminClient adminClient = this.createAdminClient();

        final ConfigResource configResource = new ConfigResource(ConfigResource.Type.TOPIC, "__delayed_message_30sec");
        List<AlterConfigOp> alterConfigOps = Arrays.asList(
                new AlterConfigOp(new ConfigEntry("delay.message.mode", "STATIC_TIME"), AlterConfigOp.OpType.SET),
                new AlterConfigOp(new ConfigEntry("delay.message.millis", "30000"), AlterConfigOp.OpType.SET)
        );
        adminClient.incrementalAlterConfigs(Collections.singletonMap(configResource, alterConfigOps)).all().get();

        adminClient.close();
    }

    @Test
    public void createTopicsTest() throws ExecutionException, InterruptedException {
        final AdminClient adminClient = this.createAdminClient();
        final NewTopic newTopic = new NewTopic("__delayed_message_anytime", 1, (short) 1);
        adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
        adminClient.close();
    }

    @Test
    public void deleteTopicsTest() throws ExecutionException, InterruptedException {
        final AdminClient adminClient = this.createAdminClient();
        adminClient.deleteTopics(Collections.singletonList("__delayed_message_anytime")).all().get();
        adminClient.close();
    }

}
