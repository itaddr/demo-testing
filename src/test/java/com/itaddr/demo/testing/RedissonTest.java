package com.itaddr.demo.testing;

import org.apache.commons.lang3.math.NumberUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.api.*;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.redisson.config.TransportMode;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RedissonTest {

    private RedissonClient redissonClient;

    private RedissonReactiveClient redissonReactiveClient;

    @Before
    public void before() throws IOException {
        /*final InputStream inputStream = Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResourceAsStream("redisson.yml"));
        String configContent = new String(Files.readFile(inputStream).getBytes(StandardCharsets.UTF_8));
        System.out.println(configContent);
        Config config = Config.fromYAML(configContent);*/

        Config config = new Config().setTransportMode(TransportMode.NIO);
        config.useSingleServer()
                .setIdleConnectionTimeout(10000)
                .setConnectTimeout(10000)
                .setTimeout(3000)
                .setRetryAttempts(3)
                .setRetryInterval(1500)
                .setPassword("Cache^by123")
                .setClientName("test-1001")
                .setAddress("redis://192.168.1.222:6386")
                .setSubscriptionsPerConnection(5)
                .setSubscriptionConnectionPoolSize(50)
                .setConnectionMinimumIdleSize(32)
                .setConnectionPoolSize(64)
                .setDatabase(0)
                .setDnsMonitoringInterval(5000);

        this.redissonClient = Redisson.create(config);
        this.redissonReactiveClient = Redisson.createReactive(config);
    }

    @After
    public void after() {
        this.redissonClient.shutdown(NumberUtils.INTEGER_ZERO, 10, TimeUnit.SECONDS);
        this.redissonClient = null;
        this.redissonReactiveClient.shutdown();
    }

    @Test
    public void test01() throws IOException {
//        LockSupport.parkNanos(this, 5L * 1000000000L);
        /*MapOptions<String, String> mapOptions = MapOptions.defaults();
        mapOptions.writer(new MapWriter<String, String>() {
            @Override
            public void write(Map<String, String> map) {

            }

            @Override
            public void delete(Collection<String> keys) {

            }
        });
        RMap<String, String> rMap = redissonClient.getMap("", new StringCodec("utf-8"), mapOptions);
        RBitSet bitSet1 = redissonClient.getBitSet("");*/


        RBatch batch = redissonClient.createBatch();
        for (int i = 0; i < 10; ++i) {

            RBucketAsync<String> bucket = batch.getBucket(String.format("20220302:test:%02d", i), StringCodec.INSTANCE);
//            bucket.setAsync(String.format("20220302:test:%03d", i));
            bucket.deleteAsync();

        }
        batch.execute();

    }

    @Test
    public void test02() throws InterruptedException, TimeoutException {
        final String service = "order";
        final String generateKeyPrefix = "distributed:lock:generateKey:" + service;
        final String keyNamespace = "distributed:key:namespace:" + service;

        final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(NumberUtils.INTEGER_ONE, (r) -> new Thread(r, "distributedUniqueKey"));

        final String sessionId = UUID.randomUUID().toString();
        final RLock lock = redissonClient.getLock(generateKeyPrefix);
        if (lock.tryLock(30, TimeUnit.SECONDS)) {
            try {
                for (int i = 1; ; ++i) {
                    final String key = keyNamespace + i;
                    final RBucket<Object> bucket = redissonClient.getBucket(key);
                    final Object value = bucket.get();
                    if (null == bucket.get()) {
                        bucket.set(sessionId, 10, TimeUnit.SECONDS);
                        break;
                    } else if (value.equals(sessionId)) {
                        bucket.expire(10, TimeUnit.SECONDS);
                        break;
                    }
                }
            } finally {
                lock.unlock();
            }
        } else {
            throw new TimeoutException("生成分布式key时需要获取的锁超时");
        }
    }

}
