package com.itaddr.demo.testing;

import com.itaddr.common.tools.utils.ByteUtil;
import io.lettuce.core.RedisClient;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.hbase.thirdparty.io.netty.channel.epoll.Epoll;
import org.apache.kerby.util.HexUtil;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.redisson.config.TransportMode;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.time.LocalDateTime;
import java.time.OffsetTime;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName SnowKeyTest
 * @Description TODO
 * @Author MyPC
 * @Date 2023/1/11 9:36
 * @Version 1.0
 */
public class SnowKeyTest {

    public void test01() {
        UUID uuid = UUID.randomUUID();
        int sequence = uuid.clockSequence();
        long timestamp = uuid.timestamp();
        int variant = uuid.variant();
        int version = uuid.version();
        long node = uuid.node();

        long leastSignificantBits = uuid.getLeastSignificantBits();
        long mostSignificantBits = uuid.getMostSignificantBits();


        Jedis jedis = null;
        SetParams params = new SetParams().nx().ex(30);
        String set = jedis.set("", "", params);


        RedisClient client = null;
        RedisCommands<String, String> sync = client.connect().sync();
        SetArgs args = new SetArgs().nx().ex(30);
        sync.set("", "", args);

        sync.watch("");
        String multi = sync.multi();

        sync.unwatch();

    }

    @Test
    public void test02() {
//        System.out.println(Pattern.compile("^(http|https)://(.*)/[0-9]+[/]?$").matcher("https://www.chengzijianzhan.com/tetris/page/1634409577604099/").find());
        LocalDateTime dateTime1 = LocalDateTime.of(2099, 12, 31, 23, 59, 59);
        LocalDateTime dateTime2 = LocalDateTime.now();
        LocalDateTime dateTime3 = LocalDateTime.of(2020, 1, 1, 8, 0, 0);
        long timestamp1 = dateTime1.toInstant(OffsetTime.now().getOffset()).toEpochMilli();
        long timestamp2 = dateTime2.toInstant(OffsetTime.now().getOffset()).toEpochMilli();
        long timestamp3 = dateTime3.toInstant(OffsetTime.now().getOffset()).toEpochMilli();

        System.out.println(timestamp1);
        System.out.println(timestamp2);
        System.out.println(timestamp3);

        System.out.println();
        System.out.printf("%d, %x, %s\n", timestamp1 - timestamp3, timestamp1 - timestamp3, Long.toBinaryString(timestamp1 - timestamp3));
        System.out.printf("%d, %x, %s\n", timestamp2 - timestamp3, timestamp2 - timestamp3, Long.toBinaryString(timestamp2 - timestamp3));

        System.out.println();
        long value1 = timestamp1 << 21L;
        long value2 = timestamp2 << 21L;
        System.out.printf("%d, %x, %s\n", value1, value1, Long.toBinaryString(value1));
        System.out.printf("%d, %x, %s\n", value2, value2, Long.toBinaryString(value2));

        System.out.println();
        System.out.printf("%d, %d\n", timestamp1 << 17, (timestamp2 / 1000) << 22);

    }

    @Test
    public void test03() throws Exception {
//        LocalDateTime now = LocalDateTime.now().minusDays(2);
//        DayOfWeek dayOfWeek = now.getDayOfWeek();
//        System.out.println(dayOfWeek.getValue());

//        System.out.println(LocalDate.parse("2022-04-27", DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay().toInstant(OffsetTime.now().getOffset()).toEpochMilli());
//        LocalDateTime start = LocalDateTime.parse("2022-04-27T08:22:32", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
//        LocalDateTime end = LocalDateTime.parse("2022-04-29T08:22:31", DateTimeFormatter.ISO_LOCAL_DATE_TIME);

//        System.out.println(Duration.between(start, end).toDays());
//        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
//        LocalDate start = LocalDate.parse("2022-05-04", DateTimeFormatter.ISO_LOCAL_DATE);
//        LocalDate end = LocalDate.parse("2022-06-07", DateTimeFormatter.ISO_LOCAL_DATE);
//        Period between = Period.between(end, start);
//        System.out.println(between.getDays());
//        System.out.println(start.toEpochDay() + " " + end.toEpochDay());
//        System.out.println(LocalDate.parse("1970-01-01", DateTimeFormatter.ISO_LOCAL_DATE).toEpochDay());

        final String cpuId = CpuKey.getCpuId();
        final int processId = CpuKey.getProcessId();
        System.out.printf("%s-%d\n", cpuId, processId);
        System.out.printf("%s%04X\n", cpuId, processId);
        System.out.println();

        byte[] cpuIdBytes = HexUtil.hex2bytes(cpuId);
        byte[] keyBytes = new byte[cpuIdBytes.length + 2];
        System.arraycopy(cpuIdBytes, 0, keyBytes, 0, cpuIdBytes.length);
        keyBytes[keyBytes.length - 2] = (byte) (processId >>> 8);
        keyBytes[keyBytes.length - 1] = (byte) processId;

        System.out.println(ByteUtil.toLowerHexString(keyBytes));
        System.out.println(Base64.getEncoder().encodeToString(keyBytes));
        System.out.println(Base64.getUrlEncoder().encodeToString(keyBytes));
    }

    @Test
    public void test04() throws InterruptedException {

        final Config config = new Config();
        config.useSentinelServers()
                .addSentinelAddress("redis://192.168.1.207:26381")
                .addSentinelAddress("redis://192.168.1.231:26381")
                .addSentinelAddress("redis://192.168.1.232:26381")
                .setMasterName("mymaster")
                .setDatabase(0)
                .setPassword("Cache^by123")
                .setCheckSentinelsList(false)
                .setSlaveConnectionMinimumIdleSize(10)
                .setSlaveConnectionPoolSize(10)
                .setMasterConnectionMinimumIdleSize(10)
                .setMasterConnectionPoolSize(10);
        config.setThreads(0).setNettyThreads(0).setTransportMode(Epoll.isAvailable() ? TransportMode.EPOLL : TransportMode.NIO);
        final RedissonClient redissonClient = Redisson.create(config);

        final String namespace = "distributed:key:test";
        final int idBits = 6;
        final int serialNoBits = 15;

        final int threadNum = 10, loopNum = 40000;

        final ConcurrentMap<Long, Collection<Long>> keyResultMap = new ConcurrentHashMap<>();
        final ConcurrentMap<Long, Long> timeResultMap = new ConcurrentHashMap<>();

        final CountDownLatch countDownLatch = new CountDownLatch(threadNum);

        for (int th = 0; th < threadNum; ++th) {
            new Thread(() -> {
                // 初始化
                final SnowKeyService gen = new SnowKeyService(new SnowKeyService.IdOperator() {
                    @Override
                    public boolean trySetId(String namespace, int id, String sessionId, long expireSec) {
                        return redissonClient.getBucket(namespace + ':' + id, StringCodec.INSTANCE).trySet(sessionId, expireSec, TimeUnit.SECONDS);
                    }

                    @Override
                    public boolean isOwnedId(String namespace, int id, String sessionId) {
                        return sessionId.equals(redissonClient.getBucket(namespace + ':' + id, StringCodec.INSTANCE).get());
                    }

                    @Override
                    public void continueId(String namespace, int id, long expireSec) {
                        redissonClient.getBucket(namespace + ':' + id, StringCodec.INSTANCE).expire(expireSec, TimeUnit.SECONDS);
                    }

                    @Override
                    public void removeId(String namespace, int id) {
                        redissonClient.getBucket(namespace + ':' + id, StringCodec.INSTANCE).delete();
                    }
                });
                gen.key(namespace);

                final long threadId = Thread.currentThread().getId();
                final Collection<Long> keys = keyResultMap.computeIfAbsent(threadId, (key) -> new HashSet<>(loopNum));

                final long beginTimeMs = System.currentTimeMillis();
                for (int i = 0; i < loopNum; ++i) {
                    long key = gen.key(namespace);
                    /*System.out.printf("key=%d, time=%d, symbol=%d, serial=%d\n", key, key >>> serialNoBits + symbolBits, (key >>> serialNoBits) & (~(-1 << symbolBits)), key & ~(-1 << serialNoBits));*/
                    keys.add(key);
                }
                final long endTimeMs = System.currentTimeMillis();
                timeResultMap.put(threadId, endTimeMs - beginTimeMs);

                try {
                    gen.destroy();
                } catch (InterruptedException ignore) {
                }
                countDownLatch.countDown();
            }).start();
        }
        countDownLatch.await();

        System.out.printf("loop=%d, size=%d, time=%dms\n", threadNum * loopNum, keyResultMap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet()).size(), timeResultMap.values().stream().mapToLong(Long::longValue).sum());

        redissonClient.shutdown(NumberUtils.INTEGER_ZERO, 30, TimeUnit.SECONDS);
    }

    @Test
    public void test05() {
        System.out.println(System.currentTimeMillis());
        long seconds1 = LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0).toEpochSecond(OffsetTime.now().getOffset());
        System.out.println(System.currentTimeMillis() / 1000L - seconds1);
        System.out.println(Double.parseDouble(String.valueOf(new Long(90953019L))));
        System.out.println((double) (System.currentTimeMillis() / 1000L - seconds1));

//        System.out.println(Double.longBitsToDouble(System.currentTimeMillis() / 1000L - seconds));
//        System.out.println(new Long(90953019L).doubleValue());

        System.out.println();
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println(~(-1L << 54));
        System.out.println(ByteUtil.toBinaryString(~(-1L << 54)));
    }

    @Test
    public void test06() {


    }

}
