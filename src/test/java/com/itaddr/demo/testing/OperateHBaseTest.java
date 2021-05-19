package com.itaddr.demo.testing;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * @Author 马嘉祺
 * @Date 2020/8/26 0026 11 00
 * @Description <p></p>
 */
public class OperateHBaseTest {
    
    private static final String HBASE_QUORUM = "hbase.zookeeper.quorum";
    private static final String HBASE_ROOTDIR = "hbase.rootdir";
    private static final String HBASE_ZNODE_PARENT = "zookeeper.znode.parent";
    
    public Configuration create() {
        Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", "172.23.101.47,172.23.101.48,172.23.101.48");
        configuration.set("hbase.zookeeper.property.clientPort", "2181");
        configuration.set("hbase.rootdir", "/hbase");
        configuration.set("zookeeper.znode.parent", "/hbase");
        return configuration;
    }
    
    public static void main(String[] args) throws IOException {
        OperateHBaseTest client = new OperateHBaseTest();
//        Configuration configuration = client.create();
//
//        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(8, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());
//        poolExecutor.prestartCoreThread();
//        Connection connection = ConnectionFactory.createConnection(configuration, poolExecutor);
//
//        client.put01(connection);
//
//        connection.close();
//
//        poolExecutor.shutdown();
        client.test01();
    }
    
    public void test01() throws IOException {
        Put put = new Put(Bytes.toBytes("0000-AABBCCDD"));
        put.addColumn(Bytes.toBytes("DATA"), Bytes.toBytes("PRODUCT_ID"), Bytes.toBytes("PRODUCT_ID"));
        put.addColumn(Bytes.toBytes("DATA"), Bytes.toBytes("MODEL"), Bytes.toBytes("MODEL"));
        put.addColumn(Bytes.toBytes("DATA"), Bytes.toBytes("VIN"), Bytes.toBytes("VIN"));
        
        CellScanner scanner = put.cellScanner();
        while (scanner.advance()) {
            Cell cell = scanner.current();
            System.out.println(Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength()));
        }
        
    }
    
    public void get01(Connection connection) throws IOException {
        Table table = connection.getTable(TableName.valueOf("test_table01"));
        
        Get get = new Get(Bytes.toBytes("0000-1111111"));
        get.addColumn(Bytes.toBytes("info"), Bytes.toBytes("name"));
        get.addColumn(Bytes.toBytes("info"), Bytes.toBytes("age"));
        
        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ONE);
//        filterList.addFilter(new ValueFilter(CompareOperator.EQUAL, new BinaryComparator(Bytes.toBytes("18"))));
        filterList.addFilter(new ColumnPrefixFilter(Bytes.toBytes("name")));
        filterList.addFilter(new ColumnPrefixFilter(Bytes.toBytes("age")));
        filterList.addFilter(new ColumnValueFilter(Bytes.toBytes("info"), Bytes.toBytes("age"), CompareOperator.EQUAL, new BinaryComparator(Bytes.toBytes("18"))));
        
        Result rs = table.get(get);
        
        String rowkey = Bytes.toString(rs.getRow());
        System.out.println("row key :" + rowkey);
        Cell[] cells = rs.rawCells();
        for (Cell cell : cells) {
            System.out.println(Bytes.toString(cell.getFamilyArray(), cell.getFamilyOffset(), cell.getFamilyLength()) + "::" + Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength()) + "::" +
                    Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength()));
        }
        
        table.close();
    }
    
    public void scan01(Connection connection) throws IOException {
        Table table = connection.getTable(TableName.valueOf("MESSAGE_UNTIED_1910"));
        Scan scan = new Scan().withStartRow(Bytes.toBytes("C899-KAFKATEST00000001-24D0F6470F4-D81A")).withStopRow(Bytes.toBytes("C899-KAFKATEST00000001-24D0F66957D-D823"));
//        scan.addColumn(Bytes.toBytes("info"), Bytes.toBytes("age"));
        
        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL);
//        filterList.addFilter(new ValueFilter(CompareOperator.EQUAL, new BinaryComparator(Bytes.toBytes("18"))));
//        filterList.addFilter(new ColumnPrefixFilter(Bytes.toBytes("name")));
//        filterList.addFilter(new ColumnPrefixFilter(Bytes.toBytes("age")));
        //filterList.addFilter(new SingleColumnValueFilter(Bytes.toBytes("info"), Bytes.toBytes("age"), CompareOperator.EQUAL, new BinaryComparator(Bytes.toBytes("18"))));
        filterList.addFilter(new SingleColumnValueExcludeFilter(Bytes.toBytes("info"), Bytes.toBytes("age"), CompareOperator.EQUAL, new BinaryComparator(Bytes.toBytes("18"))));

//        scan.setFilter(filterList);
        //scan.addColumn(Bytes.toBytes("info"), Bytes.toBytes("name"));
        //scan.addColumn(Bytes.toBytes("info"), Bytes.toBytes("age"));
        
        scan.setLimit(10);
        // hbase.client.scanner.caching 客户端每次 rpc fetch 的行数
        scan.setCaching(50);
        // 客户端每次获取的列数
        scan.setBatch(2500);
        // hbase.client.scanner.max.result.size 客户端缓存的最大字节数
        scan.setMaxResultSize(20 * 1024 * 1024);
        // 是否将正序扫描改为倒序扫描
        scan.setReversed(true);
        
        ResultScanner scanner = table.getScanner(scan);
        for (Result rs : scanner) {
            String rowKey = Bytes.toString(rs.getRow());
            System.out.println("row key: " + rowKey);
            Cell[] cells = rs.rawCells();
            for (Cell cell : cells) {
                System.out.println(Bytes.toString(cell.getFamilyArray(), cell.getFamilyOffset(), cell.getFamilyLength()) + "::"
                        + Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength()) + "::" +
                        Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength()));
            }
            System.out.println("-----------------------------------------");
        }
        scanner.close();
        table.close();
    }
    
    public void put01(Connection connection) throws IOException {
        BufferedMutatorParams params = new BufferedMutatorParams(TableName.valueOf(""));
        params.writeBufferSize(10 * 1024 * 1024); // 注意这个值，有可能导致内存溢出
        connection.getBufferedMutator(params);
        
        
        final Table table = connection.getTable(TableName.valueOf("MESSAGE_JOURNAL_01"));
        
        Put put = new Put(Bytes.toBytes("C899-KAFKATEST00000001-24D0EF9CA3E-7E7F"));
        put.addColumn(Bytes.toBytes("DATA"), Bytes.toBytes("PRODUCT_ID"), Bytes.toBytes("ProductID01"));
        put.addColumn(Bytes.toBytes("DATA"), Bytes.toBytes("MODEL"), Bytes.toBytes("1"));
        put.addColumn(Bytes.toBytes("DATA"), Bytes.toBytes("VIN"), Bytes.toBytes("KAFKATEST00000001"));
        put.addColumn(Bytes.toBytes("DATA"), Bytes.toBytes("IOTHUB_TIME"), Bytes.toBytes("2019-10-30 17:50:10.394"));
        put.addColumn(Bytes.toBytes("DATA"), Bytes.toBytes("INSERT_TIME"), Bytes.toBytes("2019-10-30 17:50:10.420"));
        put.addColumn(Bytes.toBytes("DATA"), Bytes.toBytes("PACKAGE_ID"), Bytes.toBytes("xxx-xxxx-xxxxxxxx"));
        put.addColumn(Bytes.toBytes("DATA"), Bytes.toBytes("APPLICATION_ID"), Bytes.toBytes("6"));
        put.addColumn(Bytes.toBytes("DATA"), Bytes.toBytes("MESSAGE_TYPE"), Bytes.toBytes("2"));
        put.addColumn(Bytes.toBytes("DATA"), Bytes.toBytes("TEST_FLAG"), Bytes.toBytes("0"));
        put.addColumn(Bytes.toBytes("DATA"), Bytes.toBytes("VERSION"), Bytes.toBytes("1"));
        put.addColumn(Bytes.toBytes("DATA"), Bytes.toBytes("ACTION_TYPE"), Bytes.toBytes("up"));
        put.addColumn(Bytes.toBytes("DATA"), Bytes.toBytes("RESULT_STATUS"), Bytes.toBytes("0"));
        put.addColumn(Bytes.toBytes("DATA"), Bytes.toBytes("RESULT_MSG"), Bytes.toBytes("Successful"));
        put.addColumn(Bytes.toBytes("DATA"), Bytes.toBytes("ELEMENTS_HEX"), Bytes.toBytes("0676bd1c8a018a04fa7de7b101040100150009000001696d63753d310830312e30302e313200"));
        put.addColumn(Bytes.toBytes("DATA"), Bytes.toBytes("ORIGINAL_HEX"), Bytes.toBytes("00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"));
        put.addColumn(Bytes.toBytes("DATA"), Bytes.toBytes("KAFKA_INFO"), Bytes.toBytes("{\"timestampType\":\"CreateTime\",\"partition\":\"faw-iot-datalog.ALL-1\",\"offset\":33152,\"key\":\"KAFKATEST00000001\",\"timestamp\":1572429010394}"));
        put.addColumn(Bytes.toBytes("DATA"), Bytes.toBytes("REHBASE_NUM"), Bytes.toBytes("1"));
        
        table.put(put);
        
        table.close();
    }
    
}
