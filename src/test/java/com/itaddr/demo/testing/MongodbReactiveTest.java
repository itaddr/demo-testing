package com.itaddr.demo.testing;

import com.itaddr.common.tools.utils.ByteUtil;
import com.mongodb.*;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.*;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @ClassName MongodbReactiveTest
 * @Description TODO
 * @Author MyPC
 * @Date 2023/1/12 10:00
 * @Version 1.0
 */
public class MongodbReactiveTest {

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
    public void find01() throws InterruptedException {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        final CountDownLatch awaitHolder = new CountDownLatch(3);
        final MongoCollection<Document> collection = database.getCollection("3399_delayed_message_anytime_test");

        final long beginTimeMs = System.currentTimeMillis();

        final long currentTimeMs = LocalDateTime.of(2023, 1, 31, 18, 57, 5, 624).toInstant(OffsetTime.now().getOffset()).toEpochMilli();
        final BsonDocument filter1 = new BsonDocument().append("group_id", new BsonString("3399-dynamic-time-delay-group")).append("topic", new BsonString("__3399_delayed_message_anytime")).append("partition", new BsonInt32(0));
        Flux.from(collection.countDocuments(filter1)).doOnSubscribe(subs -> subs.request(Long.MAX_VALUE)).doOnError(Throwable::printStackTrace).doFinally(type -> awaitHolder.countDown()).subscribe(count -> System.out.println("总数据量: " + count));

        final BsonDocument filter2 = new BsonDocument().append("group_id", new BsonString("3399-dynamic-time-delay-group")).append("topic", new BsonString("__3399_delayed_message_anytime")).append("partition", new BsonInt32(0))
                .append("target_time", new BsonDocument("$gte", new BsonDateTime(0L)).append("$lt", new BsonDateTime(currentTimeMs + 6 * 60 * 60 * 1000L)));
        Flux.from(collection.find(filter2)).doOnSubscribe(subs -> subs.request(Long.MAX_VALUE)).doOnError(Throwable::printStackTrace).doFinally(type -> awaitHolder.countDown()).subscribe(document -> {
            final ObjectId objectId = document.getObjectId("_id");
            final String groupId = document.getString("group_id");
            final String topic = document.getString("topic");
            final Integer partition = document.getInteger("partition");
            final Long offset = document.getLong("offset");
            final Long timestamp = document.getLong("timestamp");
            final String headersStr = document.getList("headers", Document.class, Collections.emptyList())
                    .stream().map(e -> String.format("key=%s|value=%s", e.getString("key"), ByteUtil.toBase64String(e.get("value", Binary.class).getData())))
                    .collect(Collectors.joining(", ", "[", "]"));
            final Binary key = document.get("key", Binary.class);
            final Binary value = document.get("value", Binary.class);
            final String targetTopic = document.getString("target_topic");
            final Date enterTime = document.getDate("enter_time");
            final Date targetTime = document.getDate("target_time");
            final Date createTime = document.getDate("create_time");
            System.out.printf("缓存期内的数据: objectId=%s, groupId=%s, topic=%s, partition=%d, offset=%d, timestamp=%d, headers=%s, key=%s, value=%s, targetTopic=%s, enterTime=%s, targetTime=%s, createTime=%s\n",
                    objectId.toString(), groupId, topic, partition, offset, timestamp, headersStr, ByteUtil.toBase64String(key.getData()), ByteUtil.toBase64String(value.getData()),
                    targetTopic, sdf.format(enterTime), sdf.format(targetTime), sdf.format(createTime));
        });

        final BsonDocument filter3 = new BsonDocument().append("group_id", new BsonString("3399-dynamic-time-delay-group")).append("topic", new BsonString("__3399_delayed_message_anytime")).append("partition", new BsonInt32(0))
                .append("target_time", new BsonDocument("$gte", new BsonDateTime(currentTimeMs + 6 * 60 * 60 * 1000L)));
        Flux.from(collection.find(filter3).projection(new BsonDocument("_id", new BsonInt32(0)).append("target_time", new BsonInt32(1))).sort(new BsonDocument("target_time", new BsonInt32(1))).limit(1))
                .doOnSubscribe(subs -> subs.request(Long.MAX_VALUE)).doOnError(Throwable::printStackTrace).doFinally(type -> awaitHolder.countDown())
                .subscribe(document -> System.out.printf("缓存期外的目标时间: targetTime=%s, document=%s\n", sdf.format(document.get("target_time")), document.toJson()));

        awaitHolder.await();
        final long endTimeMs = System.currentTimeMillis();
        System.out.printf("查询总耗时: %dms\n", endTimeMs - beginTimeMs);
    }

    @Test
    public void find02() throws InterruptedException {
        final CountDownLatch awaitHolder = new CountDownLatch(1);
        final MongoCollection<Document> collection = database.getCollection("3399_delayed_message_anytime_test");
        final BsonDocument filter = new BsonDocument().append("group_id", new BsonString("3399-dynamic-time-delay-group")).append("topic", new BsonString("__3399_delayed_message_anytime")).append("partition", new BsonInt32(0));

        final long beginTimeMs = System.currentTimeMillis();
        collection.countDocuments(filter).subscribe(new Subscriber<Long>() {
            @Override
            public void onSubscribe(Subscription subs) {
                subs.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Long count) {
                System.out.println("总数据量: " + count);
            }

            @Override
            public void onError(Throwable cause) {
                cause.printStackTrace();
                awaitHolder.countDown();
            }

            @Override
            public void onComplete() {
                awaitHolder.countDown();
            }
        });
        awaitHolder.await();
        final long endTimeMs = System.currentTimeMillis();

        System.out.printf("查询总耗时: %dms\n", endTimeMs - beginTimeMs);
    }

    @Test
    public void find03() throws InterruptedException {
        final CountDownLatch awaitHolder = new CountDownLatch(1);
        final MongoCollection<Document> collection = database.getCollection("3399_delayed_message_anytime_test");
        final BsonDocument filter = new BsonDocument().append("group_id", new BsonString("3399-dynamic-time-delay-group")).append("topic", new BsonString("__3399_delayed_message_anytime")).append("partition", new BsonInt32(0));

        final long beginTimeMs = System.currentTimeMillis();
        Flux.from(collection.countDocuments(filter)).subscribe(count -> System.out.println("总数据量: " + count), cause -> {
            cause.printStackTrace();
            awaitHolder.countDown();
        }, awaitHolder::countDown, subs -> subs.request(Long.MAX_VALUE));
        awaitHolder.await();
        final long endTimeMs = System.currentTimeMillis();

        System.out.printf("查询总耗时: %dms\n", endTimeMs - beginTimeMs);
    }

    @Test
    public void find04() throws InterruptedException {
        final CountDownLatch awaitHolder = new CountDownLatch(1);
        final MongoCollection<Document> collection = database.getCollection("3399_delayed_message_anytime_test");

        final BsonDocument filter = new BsonDocument().append("group_id", new BsonString("3399-dynamic-time-delay-group")).append("topic", new BsonString("__3399_delayed_message_anytime")).append("partition", new BsonInt32(0));

        final long beginTimeMs = System.currentTimeMillis();
        Flux.from(collection.countDocuments(filter)).doOnSubscribe(subs -> subs.request(Long.MAX_VALUE)).doFinally(type -> awaitHolder.countDown()).doOnError(Throwable::printStackTrace).subscribe(count -> System.out.println("总数据量: " + count));
        awaitHolder.await();
        final long endTimeMs = System.currentTimeMillis();

        System.out.printf("查询总耗时: %dms\n", endTimeMs - beginTimeMs);
    }

    @Test
    public void find05() throws InterruptedException {
        final CountDownLatch awaitHolder = new CountDownLatch(1);
        final MongoCollection<Document> collection = database.getCollection("3399_delayed_message_anytime_test");
        final AtomicLong queueLength = new AtomicLong(0);
        final BsonDocument filter = new BsonDocument().append("group_id", new BsonString("3399-dynamic-time-delay-group")).append("topic", new BsonString("__3399_delayed_message_anytime")).append("partition", new BsonInt32(0));

        final long beginTimeMs = System.currentTimeMillis();
        Flux.concat(collection.countDocuments(filter), collection.countDocuments(filter), collection.countDocuments(filter), collection.countDocuments(filter), collection.countDocuments(filter))
                .doOnSubscribe(subs -> subs.request(Long.MAX_VALUE)).doFinally(type -> awaitHolder.countDown()).doOnError(Throwable::printStackTrace).doOnComplete(() -> System.out.println("完成")).subscribe(queueLength::addAndGet);
        awaitHolder.await();
        final long endTimeMs = System.currentTimeMillis();

        System.out.printf("查询总耗时: %dms, queueLength=%d\n", endTimeMs - beginTimeMs, queueLength.get());
    }

    @Test
    public void insertOne01() throws InterruptedException {
        // 获取集合中最大偏移量
        final CountDownLatch awaitHolder1 = new CountDownLatch(1);
        final AtomicLong offsetRef = new AtomicLong();
        final MongoCollection<Document> collection = database.getCollection("3399_delayed_message_anytime_test");
        Flux.from(collection.find(new BsonDocument("offset", new BsonDocument("$ne", BsonNull.VALUE).append("$exists", BsonBoolean.TRUE))).projection(new BsonDocument("_id", new BsonInt32(0)).append("offset", new BsonInt32(1))).sort(new BsonDocument("offset", new BsonInt32(-1))).limit(1))
                .doOnSubscribe(subs -> subs.request(Long.MAX_VALUE)).doOnError(cause -> {
            cause.printStackTrace();
            offsetRef.set(-1L);
        }).doFinally(type -> awaitHolder1.countDown()).map(doc -> doc.getLong("offset")).subscribe(offsetRef::set);
        awaitHolder1.await();
        final long maxOffsetVal = offsetRef.get();
        if (-1L == maxOffsetVal) {
            return;
        }

        final int insertCount = 1;
        final CountDownLatch awaitHolder2 = new CountDownLatch(1);
        final ThreadLocalRandom random = ThreadLocalRandom.current();

        final List<Document> documentList = new ArrayList<>();
        for (int i = 0; i < insertCount; ++i) {
            final long offset = maxOffsetVal + i + 1;
            final long currentTimeMs = System.currentTimeMillis();
            final long targetTimeMs = currentTimeMs + random.nextLong(30 * 24 * 60 * 60 * 1000L) + 2 * 24 * 60 * 60 * 1000L;

            final ByteBuffer buffer = ByteBuffer.allocate(12)
                    .putInt((int) (currentTimeMs / 1000L))
                    .put((byte) 0).putShort((short) 0)
                    .putShort((short) 0)
                    .put((byte) 0).putShort((short) i);
            buffer.flip();
            final Document insertDoc = new Document()
                    .append("_id", new ObjectId(buffer))
                    .append("group_id", "3399-dynamic-time-delay-group")
                    .append("topic", "__3399_delayed_message_anytime")
                    .append("partition", 0)
                    .append("offset", offset)
                    .append("timestamp", currentTimeMs)
                    .append("headers", Arrays.asList(new Document().append("key", "headerKey1-" + offset).append("value", ("headerValue1-" + offset).getBytes(StandardCharsets.UTF_8)), new Document().append("key", "headerKey2-" + offset).append("value", ("headerValue2-" + offset).getBytes(StandardCharsets.UTF_8))))
                    .append("key", ("recordKey" + offset).getBytes(StandardCharsets.UTF_8))
                    .append("value", ("recordValue" + offset).getBytes(StandardCharsets.UTF_8))
                    .append("target_topic", "delayed_message_test")
                    .append("enter_time", new Date(currentTimeMs))
                    .append("target_time", new Date(targetTimeMs))
                    .append("create_time", new Date(currentTimeMs));
            documentList.add(insertDoc);
        }

        final long beginTimeMs = System.currentTimeMillis();
        Flux.from(mongoClient.startSession()).doOnSubscribe(subs -> subs.request(Long.MAX_VALUE)).doOnError(Throwable::printStackTrace).subscribe(session -> {
            // 开启事务
            session.startTransaction(TransactionOptions.builder().maxCommitTime(60L, TimeUnit.SECONDS).readPreference(ReadPreference.primaryPreferred()).writeConcern(WriteConcern.MAJORITY).readConcern(ReadConcern.SNAPSHOT).build());
            Flux.from(collection.insertMany(documentList)).doOnSubscribe(subs -> subs.request(Long.MAX_VALUE)).doOnError((cause) -> {
                cause.printStackTrace();
                // 回滚事务
                Flux.from(session.abortTransaction()).doOnError(Throwable::printStackTrace).doFinally(type -> awaitHolder2.countDown()).subscribe(v -> session.close());
            }).doOnComplete(() -> {
                // 提交事务
                Flux.from(session.commitTransaction()).doOnError(Throwable::printStackTrace).doFinally(type -> awaitHolder2.countDown()).subscribe(v -> session.close());
            }).subscribe(result -> result.getInsertedIds().forEach((idx, objectId) -> {
                final Document document = documentList.get(idx);
                if (null != document) {
                    System.out.printf("保存数据成功: idx=%d, %s\n", idx, document.append("_id", objectId.asObjectId()).toJson());
                } else {
                    System.out.printf("保存数据失败: idx=%d\n", idx);
                }
            }));
        });
        awaitHolder2.await();
        final long endTimeMs = System.currentTimeMillis();

        System.out.printf("插入数据耗时: %dms\n", endTimeMs - beginTimeMs);
    }

    @Data
    @EqualsAndHashCode
    public static class Header {

        private String key;

        private byte[] value;

        public Header() {
        }

        public Header(String key, byte[] value) {
            this.key = key;
            this.value = value;
        }

    }

}
