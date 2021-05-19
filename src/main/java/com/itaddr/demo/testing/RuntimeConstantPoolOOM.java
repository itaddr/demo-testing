package com.itaddr.demo.testing;

import java.util.HashSet;
import java.util.Set;

/**
 * VM Args: -XX:PermSize=6M -XX:MaxPermSize=6M
 * 只会在在jdk6-版本下才能看到效果
 * @Author 马嘉祺
 * @Date 2021/3/2 0002 10 16
 * @Description <p></p>
 */
public class RuntimeConstantPoolOOM {
    
    public static void main(String[] args) {
        // 使用Set保持着常量池引用， 避免Full GC回收常量池行为
        Set<String> set = new HashSet<>();
        // 在short范围内足以让6MB的PermSize产生OOM了
        short i = 0;
        while (true) {
            set.add(String.valueOf(i++).intern());
        }
    }
    
}
