package com.itaddr.demo.testing;

import io.lettuce.core.RedisClient;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.sync.RedisCommands;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.UUID;

/**
 * @Author 马嘉祺
 * @Date 2020/11/1 0001 17 00
 * @Description <p></p>
 */
public class Test {
    
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
    
}
