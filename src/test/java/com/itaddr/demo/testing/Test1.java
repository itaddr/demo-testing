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
import org.redisson.config.Config;
import org.redisson.config.TransportMode;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.time.LocalDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author 马嘉祺
 * @Date 2020/11/1 0001 17 00
 * @Description <p></p>
 */
public class Test1 {

    public void test1() {
        int a, b, c;
        a = 3;
        b = 4;
        c = a + b;
    }

    public int test2() {
        int a, b;
        a = 3;
        b = 4;
        return a + b;
    }

    public int test2(int arg) {
        int a, b;
        a = arg;
        b = 4;
        return a + b;
    }

    public static void main(String[] args) {
        byte b = (byte) 200;
        short s = (short) 65535;
        char c = 65535;
        int i = 0xffffffff;
        long l = 0x100000000L;

        b = (byte) (b >>> 4);
        s = (short) (s >>> 8);
        c >>>= 10;
        i >>>= 32;
        l >>>= 64;
        System.out.printf("b=%d, s=%d, c=%c, i=%x, l=%x\n", b, s, c, i, l);
    }

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
    public void tet02() {
        DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd HH:mm:ss").toFormatter();

        int roleType = 1, timeType = 2, ValidDays = 30;

        LocalDateTime createRoleTime = LocalDateTime.parse("2022-01-25 14:36:56", dateTimeFormatter);
        LocalDateTime currentTime = LocalDateTime.parse("2022-02-24 14:07:00", dateTimeFormatter);

        final LocalDateTime startTime = LocalDateTime.parse("2022-01-25 00:00:00", dateTimeFormatter);
        final LocalDateTime staticFinishTime = LocalDateTime.parse("2022-01-28 23:59:59", dateTimeFormatter);
        final LocalDateTime finishTime;

        if (1 == roleType) { // 新角色活动

            // 校验角色是否可以参与活动
            if (createRoleTime.compareTo(startTime) < 0 || createRoleTime.compareTo(staticFinishTime) > 0) {
                System.err.println("创角时间不合法");
                return;
            }

            if (1 == timeType) { // 静态时间
                finishTime = staticFinishTime;
            } else if (2 == timeType) { // 动态时间
                finishTime = createRoleTime.plusDays(ValidDays);
            } else { // 未知类型
                System.err.println("未知时间");
                return;
            }

        } else { // 活跃/老角色活动
            finishTime = staticFinishTime;
        }

        // 校验活动时间是否过期
        if (currentTime.compareTo(startTime) >= 0 && currentTime.compareTo(finishTime) <= 0) {
            System.out.println("活动可参与");
            return;
        }

        System.err.println("活动已过期");
    }

    @Test
    public void test03() {
//        System.out.println(ZoneOffset.UTC);
//        System.out.println(ZoneOffset.systemDefault().getClass());
//        System.out.println(ZoneOffset.systemDefault());

//        System.out.println(OffsetTime.MAX.getOffset());
//        System.out.println(LocalDateTime.ofInstant(new Date().toInstant(), OffsetTime.now().getOffset()));

        System.out.println("DAY_OF_QUARTER: " + LocalDateTime.now().get(IsoFields.DAY_OF_QUARTER));
        System.out.println("QUARTER_OF_YEAR: " + LocalDateTime.now().get(IsoFields.QUARTER_OF_YEAR));
        System.out.println("WEEK_OF_WEEK_BASED_YEAR: " + LocalDateTime.now().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
        System.out.println("WEEK_BASED_YEAR: " + LocalDateTime.now().get(IsoFields.WEEK_BASED_YEAR));

        System.out.println(LocalDateTime.now());
        System.out.println(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS));
//        System.out.println(LocalDateTime.now().get(IsoFields.WEEK_BASED_YEARS));
//        System.out.println(LocalDateTime.now().get(IsoFields.QUARTER_YEARS));
    }

    @Test
    public void test04() {
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
    public void test05() throws Exception {
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
    public void test06() throws InterruptedException {

        Config config = new Config().setTransportMode(Epoll.isAvailable() ? TransportMode.EPOLL : TransportMode.NIO);
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
        final RedissonClient redissonClient = Redisson.create(config);

        final String namespace = "test";
        final int serialNoBits = 16;
        final int symbolBits = 5;
        final DistributedKey gen = new DistributedKey(redissonClient);
        long beginMillis = System.currentTimeMillis();
        for (int i = 0; i < 20000; ++i) {
            long key = gen.key(namespace, serialNoBits, symbolBits);
            System.out.printf("key=%d, time=%d, symbol=%d, serial=%d\n", key, key >>> serialNoBits + symbolBits, (key >>> symbolBits) & (~(-1 << serialNoBits)), key & (~(-1 << symbolBits)));
        }
        System.out.println(System.currentTimeMillis() - beginMillis);
        Thread.sleep(2000);
        gen.destroy();
//        System.out.println(new Date((1734955580103694L >>> 20) * 1000));
//        System.out.println(ByteUtil.toBinaryString(1734955580103694L & 0b11111111111111111111L));
//        System.out.println(ByteUtil.toBinaryString(1734955579286541L & 0b11111111111111111111L));
//        System.out.println(ByteUtil.toBinaryString(1734955578454023L & 0b11111111111111111111L));
//        System.out.println(ByteUtil.toBinaryString(1734955577649166L & 0b11111111111111111111L));
//        System.out.println(ByteUtil.toBinaryString(1734955576851464L & 0b11111111111111111111L));
//        System.out.println(ByteUtil.toBinaryString(1734955575973902L & 0b11111111111111111111L));
//        System.out.println(ByteUtil.toBinaryString(1734955575218183L & 0b11111111111111111111L));
//        System.out.println(ByteUtil.toBinaryString(1734955574463502L & 0b11111111111111111111L));
//        System.out.println(ByteUtil.toBinaryString(1734955573669902L & 0b11111111111111111111L));
//        System.out.println(ByteUtil.toBinaryString(1734955572838472L & 0b11111111111111111111L));
//        System.out.println(ByteUtil.toBinaryString(1734955558616077L & 0b11111111111111111111L));
//        System.out.println(ByteUtil.toBinaryString(1734955557724173L & 0b11111111111111111111L));
//        System.out.println(ByteUtil.toBinaryString(1734955556938765L & 0b11111111111111111111L));
//        System.out.println(ByteUtil.toBinaryString(1734955556163597L & 0b11111111111111111111L));
//        System.out.println(ByteUtil.toBinaryString(1734955555343437L & 0b11111111111111111111L));
//        System.out.println(ByteUtil.toBinaryString(1734955554608135L & 0b11111111111111111111L));
//        System.out.println(ByteUtil.toBinaryString(1734955553743950L & 0b11111111111111111111L));
//        System.out.println(ByteUtil.toBinaryString(1734955552915464L & 0b11111111111111111111L));
//        System.out.println(ByteUtil.toBinaryString(1734955552124936L & 0b11111111111111111111L));
//        System.out.println(ByteUtil.toBinaryString(1734955551344653L & 0b11111111111111111111L));
//        System.out.println(ByteUtil.toBinaryString(1734955525801992L & 0b11111111111111111111L));
//        System.out.println(ByteUtil.toBinaryString(1734955524936718L & 0b11111111111111111111L));

        redissonClient.shutdown(NumberUtils.INTEGER_ZERO, 15, TimeUnit.SECONDS);
    }

}
