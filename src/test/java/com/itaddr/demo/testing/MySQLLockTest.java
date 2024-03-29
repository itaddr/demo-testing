package com.itaddr.demo.testing;

import org.junit.Test;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @ClassName MySQLLockTest
 * @Description TODO
 * @Author MyPC
 * @Date 2022/8/30 13:48
 * @Version 1.0
 */
public class MySQLLockTest {

    private Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        final String jdbcUrl = "jdbc:mysql://192.168.0.35:3307/3399_maindb?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=GMT%2B8&nullCatalogMeansCurrent=true&allowPublicKeyRetrieval=true";
        final String jdbcUser = "web3399";
        final String jdbcPassword = "BlessBy^1101";
        return DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
    }

    @Test
    public void test01() throws Exception {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int pgameId = 56;
        int areaId = 1;
        String areaName = "1";
        Date createAreaTime = new Date();

        final Connection connection = this.getConnection();
        try {
            // 开启事务
            connection.setAutoCommit(false);

            // 执行一次更新，利用行锁实现分布式锁效果
            PreparedStatement statement = connection.prepareStatement("UPDATE parent_game_area SET area_name = ?, create_area_time = IFNULL(create_area_time, ?) WHERE parent_game_id = ? AND area_id = ?");
            statement.setString(1, areaName);
            statement.setTimestamp(2, new java.sql.Timestamp(createAreaTime.getTime()));
            statement.setInt(3, pgameId);
            statement.setInt(4, areaId);
            statement.execute();
            statement.close();
            System.out.printf("test01更新完成: areaName = %s, createAreaTime = %s\n", areaName, sdf.format(createAreaTime));

//            Thread.sleep(5000);

            // 执行一次查询，读取到的是最新数据
            statement = connection.prepareStatement("SELECT * FROM parent_game_area WHERE parent_game_id = ? AND area_id = ?");
            statement.setInt(1, pgameId);
            statement.setInt(2, areaId);
            final ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                areaName = resultSet.getString("area_name");
                createAreaTime = resultSet.getTimestamp("create_area_time");
                System.out.printf("test01查询完成: areaName = %s, createAreaTime = %s\n", areaName, sdf.format(createAreaTime));
            }
            statement.close();

            // 阻塞当前线程，继续持有行锁
            Thread.sleep(15000);

            // 提交事务，并且释放锁
            connection.commit();
            System.out.println(System.currentTimeMillis() / 1000);
        } catch (Exception e) {
            connection.rollback();
            e.printStackTrace();
        } finally {
            connection.close();
        }
    }

    @Test
    public void test02() throws Exception {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int pgameId = 56;
        int areaId = 1;
        String areaName = "2";
        Date createAreaTime = new Date();

        final Connection connection = this.getConnection();
        try {
            // 开启事务
            connection.setAutoCommit(false);

            // 执行一次更新，利用行锁实现分布式锁效果（锁争用，被阻塞）
            PreparedStatement statement = connection.prepareStatement("UPDATE parent_game_area SET area_name = ?, create_area_time = IFNULL(create_area_time, ?) WHERE parent_game_id = ? AND area_id = ?");
            statement.setString(1, areaName);
            statement.setTimestamp(2, new java.sql.Timestamp(createAreaTime.getTime()));
            statement.setInt(3, pgameId);
            statement.setInt(4, areaId);
            statement.execute();
            System.out.printf("test01更新完成: areaName = %s, createAreaTime = %s\n", areaName, sdf.format(createAreaTime));

            // 执行一次查询
            statement = connection.prepareStatement("SELECT * FROM parent_game_area WHERE parent_game_id = ? AND area_id = ?");
            statement.setInt(1, pgameId);
            statement.setInt(2, areaId);
            final ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                areaName = resultSet.getString("area_name");
                createAreaTime = resultSet.getTimestamp("create_area_time");
                System.out.printf("test01查询完成: areaName = %s, createAreaTime = %s\n", areaName, sdf.format(createAreaTime));
            }
            statement.close();

            // 提交事务，并释放行锁
            connection.commit();
            System.out.println(System.currentTimeMillis() / 1000);
        } catch (Exception e) {
            connection.rollback();
            e.printStackTrace();
        } finally {
            connection.close();
        }
    }

}
