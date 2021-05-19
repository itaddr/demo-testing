package com.itaddr.demo.testing;

import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

/**
 * @Author 马嘉祺
 * @Date 2020/7/6 0006 09 59
 * @Description <p></p>
 */
public class Sl4jLoggerTest {
    
    private LocationAwareLogger logger = (LocationAwareLogger) LoggerFactory.getLogger(Sl4jLoggerTest.class);
    
    @Test
    public void test01() {
        logger.info("ddd");
        
        logger.log(null, logger.getClass().getName(), LocationAwareLogger.INFO_INT, "{} --- {}", new Object[]{"aa", "bb"}, new Exception("TestException"));
    }
    
}
