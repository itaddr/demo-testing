package com.itaddr.demo.testing;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.EventType;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Scanner;

/**
 * @ClassName MySQLBinLogTest
 * @Description TODO
 * @Author MyPC
 * @Date 2023/2/3 10:21
 * @Version 1.0
 */
public class MySQLBinLogTest {

    public static void main(String[] args) throws IOException {

        final BinaryLogClient client = new BinaryLogClient("localhost", 3306, "mysql", "root", "root");
        client.registerEventListener(event -> {
            if (event.getData() instanceof TableMapEventData) {
                System.out.println("tableMap: " + event.toString());
//                final TableMapEventData tableMap = event.getData();
//                System.out.printf("tableMap: database=%s, tableId=%d, tableName=%s, columnMetadata=%s\n", tableMap.getDatabase(), tableMap.getTableId(), tableMap.getTable(), Arrays.toString(tableMap.getColumnMetadata()));
            } else if (EventType.isWrite(event.getHeader().getEventType())) {
                System.out.println("write: " + event.toString());
//                final WriteRowsEventData writeRows = (WriteRowsEventData) data;
//                System.out.printf("writeRows: tableId=%d, includedCol=%s\n", writeRows.getTableId(), Arrays.toString(writeRows.getIncludedColumns().stream().toArray()));
//                for (Serializable[] row : writeRows.getRows()) {
//                    System.out.println("\t" + Arrays.toString(row));
//                }
            } else if (EventType.isUpdate(event.getHeader().getEventType())) {
                System.out.println("update: " + event.toString());
//                final UpdateRowsEventData updateRows = (UpdateRowsEventData) data;
//                System.out.printf("updateRows: tableId=%d, includedColumns=%s, includedColumnsBeforeUpdate=%s\n", updateRows.getTableId(), Arrays.toString(updateRows.getIncludedColumns().stream().toArray()), Arrays.toString(updateRows.getIncludedColumnsBeforeUpdate().stream().toArray()));
//                for (Map.Entry<Serializable[], Serializable[]> row : updateRows.getRows()) {
//                    System.out.printf("\tbefore=%s, after=%s\n", Arrays.toString(row.getKey()), Arrays.toString(row.getValue()));
//                }
            } else if (EventType.isDelete(event.getHeader().getEventType())) {
//                final DeleteRowsEventData deleteRows = (DeleteRowsEventData) data;
                System.out.println("delete: " + event.toString());
//                System.out.printf("deleteRows: tableId=%d, includedCol=%s\n", deleteRows.getTableId(), Arrays.toString(deleteRows.getIncludedColumns().stream().toArray()));
//                for (Serializable[] row : deleteRows.getRows()) {
//                    System.out.println("\t" + Arrays.toString(row));
//                }
            } else {
                System.out.println("unknown: " + event.toString());
            }
        });
        final Thread daemonThread = new Thread(() -> {
            System.out.println(">>> 开始binlog监听");
            try {
                client.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        daemonThread.start();

        final Scanner scanner = new Scanner(System.in);
        for (String readLine; scanner.hasNext() && null != (readLine = scanner.nextLine()); ) {
            readLine = StringUtils.trim(readLine);
            if ("exit".equals(readLine) || "quit".equals(readLine) || "stop".equals(readLine)) {
                break;
            }
        }

        client.disconnect();
        System.out.println(">>> binlog监听已释放");
    }

}
