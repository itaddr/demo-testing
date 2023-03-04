package com.itaddr.demo.testing;

import com.mongodb.ConnectionString;
import com.mongodb.client.*;
import com.mongodb.client.result.InsertManyResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.*;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName MongoDBTest
 * @Description TODO
 * @Author MyPC
 * @Date 2023/1/11 17:05
 * @Version 1.0
 */
public class MongodbSyncTest {

    private MongoClient mongoClient;

    private MongoDatabase database;

    @Before
    public void before() {
        final ConnectionString connectionString = new ConnectionString("mongodb://fxuser:kidYe0519@192.168.1.231:27027,192.168.1.232:27027,192.168.1.207:27027/3399_log");
        this.mongoClient = MongoClients.create(connectionString);
        this.database = mongoClient.getDatabase(Objects.requireNonNull(connectionString.getDatabase()));
        System.out.println(database.getName());
        System.out.println(".............. mongoClient init success ..............");
    }

    @After
    public void after() {
        mongoClient.close();
        System.out.println(".............. mongoClient close success ..............");
    }

    @Test
    public void find01() {
        final MongoCollection<Document> collection = database.getCollection("3399_delayed_message_anytime");
        final BsonDocument bson = new BsonDocument();
        bson.put("group_id", new BsonString("3399-dynamic-time-delay-group"));
        bson.put("topic", new BsonString("__3399_delayed_message_anytime"));
        bson.put("partition", new BsonInt32(0));
        final BsonDateTime bsonDateTime = new BsonDateTime(1672243199000L);
        bson.put("target_time", new BsonDocument("$lt", bsonDateTime));

        System.out.println("======================================================================================");
        System.out.println(bsonDateTime);

        final FindIterable<Document> documents = collection.find(bson);
        System.out.println();
        System.out.println();
        for (Document document : documents) {
            final Binary key = (Binary) document.get("key");
            key.getData();

            System.out.printf("id=%s, groupId=%s, topic=%s, partition=%d, offset=%d, timestamp=%s, key=%s, target_time=%s\n",
                    document.getObjectId("_id").toString(), document.getString("group_id"), document.getString("topic"), document.getInteger("partition"), document.getLong("offset"),
                    document.getLong("timestamp"), key, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(document.getDate("target_time")));
        }
        System.out.println("======================================================================================");

    }

    @Test
    public void insertMany02() {
        final MongoCollection<Document> collection = database.getCollection("3399_delayed_message_anytime");
        InsertManyResult insertManyResult = collection.insertMany(Collections.emptyList());
        Map<Integer, BsonValue> insertedIds = insertManyResult.getInsertedIds();

    }

    @Data
    public static class Demo {

        private int code;

        public Demo(int code) {
            this.code = code;
        }

    }

    @Data
    @EqualsAndHashCode
    public static class DynamicDelayedMessageDTO {

        private ObjectId id;

        private String groupId;

        private String topic;

        private Integer partition;

        private Long offset;

        private Long timestamp;

        private List<Header> headers;

        private byte[] key;

        private byte[] value;

        private String targetTopic;

        private Date enterTime;

        private Date targetTime;

        private Date createTime;

    }

    @Data
    @EqualsAndHashCode
    public static class Header {

        private String key;

        private byte[] value;

        public Header() {
        }

        public Header(org.apache.kafka.common.header.Header header) {
            if (null == header) {
                return;
            }
            this.key = header.key();
            this.value = header.value();
        }

        public Header(String key, byte[] value) {
            this.key = key;
            this.value = value;
        }

    }

}
