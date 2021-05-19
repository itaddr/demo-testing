package com.itaddr.demo.testing;

import java.util.ArrayList;
import java.util.List;

/**
 * VM: Args: -Xms10m -Xmx10m -XX:+HeapDumpOnOutOfMemoryError
 * @Author 马嘉祺
 * @Date 2021/3/2 0002 09 44
 * @Description <p></p>
 */
public class HeapOOM {
    
    public static void main(String[] args) {
        List<HeapOOM> list = new ArrayList<>();
        while (true) {
            list.add(new HeapOOM());
        }
    }
    
}
