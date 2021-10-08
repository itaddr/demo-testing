package com.itaddr.demo.testing;

import io.minio.MinioClient;
import org.junit.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @Title null.java
 * @Package com.itaddr.demo.testing
 * @Author 马嘉祺
 * @Date 2021/6/25 14:59
 * @Description
 */
public class MinIOTest {

    @Test
    public void createBucket() throws Exception {
        MinioClient client = new MinioClient("http://192.168.1.133:9006", "minioadmin", "minioadmin");

        if (!client.bucketExists("admaterial")) {
            client.makeBucket("admaterial");
        }
    }

    @Test
    public void setBucketPolicy() throws Exception {
        MinioClient client = new MinioClient("http://192.168.1.133:9006", "minioadmin", "minioadmin");
    }

    @Test
    public void putObject() throws Exception {
        MinioClient client = new MinioClient("http://192.168.1.133:9006", "minioadmin", "minioadmin");
        Path path = Paths.get("D:\\Backup\\桌面\\RBAC设计材料\\用户组与角色关系.png");
        System.out.println(Files.exists(path));

        try (InputStream is = Files.newInputStream(path)) {
            client.putObject("admaterial", "222222222222222.png", is, Files.size(path), null, null, "image/png");
        }
    }

    @Test
    public void getObject() throws Exception {
        MinioClient client = new MinioClient("http://192.168.1.133:9006", "minioadmin", "minioadmin");

        Path path = Paths.get("D:\\workspaces\\javaspaces\\ads-ui\\public\\upload\\operate\\material\\_7URVyxbZlpj2RpdMbpX2yUc47I=.png");
        System.out.println(Files.exists(path));

        InputStream inputStream = Files.newInputStream(path);
        client.putObject("admaterial", "测试", inputStream, Files.size(path), null, null, "image/png");
        inputStream.close();
    }

}
