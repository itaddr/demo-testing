package com.itaddr.demo.testing;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Title null.java
 * @Package com.itaddr.demo.testing
 * @Author 马嘉祺
 * @Date 2021/10/27 16:12
 * @Description
 */
@Data
@Accessors(chain = true)
public class Geo {

    /**
     * 1:省、2：市、3：区.
     */
    private Integer level;
    /**
     * 上级地理位置code.
     */
    private Integer parentCode;
    /**
     * 地理位置code.
     */
    private Integer code;
    /**
     * 地理位置名称.
     */
    private String name;

}
