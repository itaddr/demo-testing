package com.itaddr.demo.testing.queue;

import com.mongodb.client.*;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName MongDBDelayedQueue
 * @Description TODO
 * @Author MyPC
 * @Date 2022/12/23 18:04
 * @Version 1.0
 */
public class MongoDBDelayedQueue<E extends Delayed> implements DelayedQueue<E> {

    private final MongoClient mongoClient;

    public MongoDBDelayedQueue() {
        this.mongoClient = MongoClients.create("mongodb://fxuser:kidYe0519@192.168.1.231:27027,192.168.1.232:27027,192.168.1.207:27027/3399_log");
        final MongoDatabase database = mongoClient.getDatabase("3399_log");

        final MongoCollection<Document> collection = mongoClient.getDatabase("3399_log").getCollection("3399_delayed_message_anytime");

        final BsonDocument bson = new BsonDocument();
        bson.put("", new BsonString(""));
        final FindIterable<Document> documents = collection.find(bson);
        for (Document document : documents) {

        }

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public DelayedQueue<E> offer(E... elements) {
        return null;
    }

    @Override
    public DelayedQueue<E> offer(Collection<E> elements) {
        return null;
    }

    @Override
    public E poll() throws InterruptedException {
        return null;
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }

    @Override
    public Collection<E> polls() throws InterruptedException {
        return null;
    }

    @Override
    public Collection<E> polls(long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }

    @Override
    public E peek() {
        return null;
    }

    @Override
    public Long peekTargetTime() {
        return null;
    }

    @Override
    public DelayedQueue<E> destroy() {

        return this;
    }

}
