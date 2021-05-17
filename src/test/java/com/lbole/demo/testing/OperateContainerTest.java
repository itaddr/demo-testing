package com.lbole.demo.testing;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author 马嘉祺
 * @Date 2020/8/13 0013 10 30
 * @Description <p></p>
 */
public class OperateContainerTest {
    
    @Test
    public void test01() {
        Map<String, String> map = new HashMap<>();
        map.put("4", "is four");
        map.put("5", "is five");
        map.put(null, "is null");
        map.put("1", "is one");
        map.put("2", "is two");
        map.put("3", "is three");
        map.put("ddf", "is ddf");
        map.put("aca", "is aca");
        System.out.println(map.get(null));
        System.out.println(map.toString());
    }
    
}
