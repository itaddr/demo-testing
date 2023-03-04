package com.itaddr.demo.testing;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.itaddr.common.tools.utils.ByteUtil;
import com.itaddr.common.tools.utils.CodecUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

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

    @Test
    public void tet01() {
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
    public void test02() {
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
    public void test03() {
        // 15 ~ 25
//        double avgVal = 10, minVal = 15, maxVal = 25;
//        final BigDecimal avgDec = new BigDecimal(String.valueOf(avgVal));
//        double min = Double.MAX_VALUE, max = 0;
//        for (int i = 0; i < 10; ++i) {
//            double val = ThreadLocalRandom.current().nextGaussian();
//            if (val < min) {
//                min = val;
//            }
//            if (val > max) {
//                max = val;
//            }
//            final BigDecimal randVal = new BigDecimal(String.valueOf(val));
//            System.out.println(randVal.multiply(new BigDecimal(String.valueOf(Math.sqrt(maxVal - minVal)))).add(avgDec).setScale(0, BigDecimal.ROUND_HALF_UP));
//        }
//        System.out.printf("min = %f, max = %f\n", min, max);

        final BigDecimal avgValue = new BigDecimal(150);
        final BigDecimal minValue = new BigDecimal(100);
        final BigDecimal maxValue = new BigDecimal(200);

        for (int i = 0; i < 100; ++i) {
            // 获取标准的动态分布随机值
            final BigDecimal standard = new BigDecimal(String.valueOf(ThreadLocalRandom.current().nextGaussian()));
            // 最大值减去最小值然后开方获取标准值的缩放比例
            final BigDecimal sqrt = new BigDecimal(String.valueOf(Math.sqrt(maxValue.subtract(minValue).doubleValue())));
            // 标准值乘以缩放比例，然后加上平均值
            BigDecimal resultValue = standard.multiply(sqrt).add(avgValue);
            resultValue = resultValue.compareTo(minValue) < 0 ? minValue : resultValue;
            resultValue = resultValue.compareTo(maxValue) > 0 ? maxValue : resultValue;
            System.out.printf("standard=%f, sqrt=%f, result=%f\n", standard.doubleValue(), sqrt.doubleValue(), resultValue.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        }

    }

    @Test
    public void test04() {
        char[] ee = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                '-'
        };
        System.out.println((int) '-');
        System.out.println((int) '0');
        System.out.println((int) 'A');
        System.out.println((int) 'a');
        /*final Pattern pattern = Pattern.compile("^__delayed_message_[0-9]+(sec|min|hour|day)$");
        System.out.println(pattern.matcher("__delayed_message_dd").matches());
        System.out.println(pattern.matcher("__delayed_message_33").matches());
        System.out.println(pattern.matcher("__delayed_message_01d").matches());
        System.out.println(pattern.matcher("__delayed_message_01min").matches());*/
        // 15 351201  42
        byte[] bytes = {15, 0};
        bytes = ArrayUtils.addAll(bytes, "351201".getBytes(StandardCharsets.UTF_8));
        bytes = ArrayUtils.add(bytes, (byte) 0);
        bytes = ArrayUtils.addAll(bytes, "3977963503979175936".getBytes(StandardCharsets.UTF_8));
        bytes = ArrayUtils.add(bytes, (byte) 0);
        bytes = ArrayUtils.add(bytes, (byte) 42);
        System.out.println(Base64.getUrlEncoder().encodeToString(CodecUtil.md5(bytes)));
    }

    @Test
    public void test05() {
        final String appSecret = "7ea83effc511ab2a21665b2b13ff2f82565ac2a0"; // 签名密钥
        final JSONObject beans = JSON.parseObject("{\n" +
                "  \"gameId\": 1,\n" +
                "  \"timestamp\": 16654344656,\n" +
                "  \"username\": \"98504983\",\n" +
                "  \"areaId\": \"804938\",\n" +
                "  \"areaName\": \"S001\",\n" +
                "  \"roleId\": \"2202392039483904\",\n" +
                "  \"roleName\": \"superman\",\n" +
                "  \"roleExp\": \"\",\n" +
                "  \"roleLevel\": 99,\n" +
                "  \"changeTime\": 16654344656,\n" +
                "  \"roleVip\": \"\",\n" +
                "  \"ucid\": \"7438ac98f7de9387d89f7c6bb764de84\",\n" +
                "  \"sign\": \"3e521885585590de1d7923a4610a9c4d\"\n" +
                "}");

        final String plaintext = beans.entrySet().stream().filter(e -> !"sign".equals(e.getKey())).sorted(Map.Entry.comparingByKey()).map(e -> e.getKey() + '=' + e.getValue()).collect(Collectors.joining("&", StringUtils.EMPTY, appSecret)); // 拼接明文
        System.out.println(plaintext); // 签名明文

        final String sign = ByteUtil.toLowerHexString(CodecUtil.md5(plaintext.getBytes(StandardCharsets.UTF_8)));
        System.out.println(sign); // 签名结果
    }

    @Test
    public void test06() {
        try (RandomAccessFile accessFile = new RandomAccessFile("D:\\plaintext_test.txt", "rw")) {
            System.out.println(accessFile.length());
            final byte[] buffers = new byte[1024 * 1024];
            for (int len; (len = accessFile.read(buffers)) > 0; ) {
                System.out.println(new String(buffers, 0, len, StandardCharsets.UTF_8));
            }
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*try (final FileChannel fileChannel = FileChannel.open(Paths.get("D:\\plaintext_test.txt"), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            System.out.println(fileChannel.size());
            final ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 1024);
            for (int len; (len = fileChannel.read(byteBuffer)) > 0; byteBuffer.clear()) {
                System.out.println(new String(byteBuffer.array(), 0, len, StandardCharsets.UTF_8));
            }
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    @Test
    public void test07() {
        final long oneDayTimeMs = 24 * 60 * 60 * 1000L;
        final long timeZoneMs = TimeZone.getDefault().getRawOffset();
        final long currentTimeMs = System.currentTimeMillis();

        final long onlyTimeMs = currentTimeMs % oneDayTimeMs;
        final long timeZoneVal = onlyTimeMs >= timeZoneMs ? oneDayTimeMs : 0L;
        final long onlyDateMs = currentTimeMs - onlyTimeMs - timeZoneMs + timeZoneVal;

        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        System.out.printf("start = %s, end = %s\n", sdf.format(new Date(onlyDateMs)), sdf.format(new Date(onlyDateMs + oneDayTimeMs - 1L)));

        final long baseTimeMs = LocalDateTime.of(2023, 1, 1, 0, 0, 0, 0).toInstant(OffsetTime.now().getOffset()).toEpochMilli();
        System.out.printf("baseTimeMs=%d, %d", baseTimeMs, currentTimeMs - baseTimeMs);
    }

}
