package com.hscloud.hs.cost.account.deptCost;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.Date;

/**
 * @author pc
 * @date 2024/9/24
 */
@Slf4j
public class PrepareStatementUtil {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {

        // 注册 MySQL 驱动程序
        Class.forName("com.mysql.cj.jdbc.Driver");

        String querySql = "SELECT `id`, `period`, `code`, `busi_code`, `dept_id`, `user_id`, `imputation_dept_id`, `value`, `source_dept`, `zdys`, `brks`, " +
                "`kzys`, `mate_flag`, `created_date`, `tenant_id`, `ward` FROM `hsx_cost`.`kpi_item_result_test`";
        String insertSql = "INSERT INTO `hsx_cost`.dc_kpi_item_result_copy( `origin_id`, `task_id`, `copy_date`, `period`, `code`, `busi_code`, `dept_id`, `user_id`, `imputation_dept_id`, " +
                "`value`, `source_dept`, `zdys`, `brks`, `kzys`, `mate_flag`, `created_date`, `tenant_id`, `ward`) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        int queryFetchSize = 5000;
        int i = 0;
        int insertBatchSize = 10000;
        // 获取到现在的时间  类型是java.Util.Date
        Date date = new Date();
        // 把现在的时间改为long类型
        long time = date.getTime();
        // 因为java.sql.Date接受的是一个long类型的直接往里面传参即可
        java.sql.Date date1 = new java.sql.Date(time);
        log.info("开始查询 queryFetchSize:{} insertBatchSize：{}", queryFetchSize, insertBatchSize);

        Connection connection = DriverManager.getConnection("jdbc:mysql://192.168.9.135:3306/hsx_cost?" +
                        "characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&useJDBCCompliantTimezoneShift=true" +
                        "&useLegacyDatetimeCode=false&serverTimezone=GMT%2B8&allowMultiQueries=true&allowPublicKeyRetrieval=true" +
                        "&rewriteBatchedStatements=true&useCursorFetch=true",
                "root", "Cc123@leo");
        connection.setAutoCommit(false);//取消自动提交
        PreparedStatement queryPS = connection.prepareStatement(querySql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        queryPS.setFetchSize(queryFetchSize);
        queryPS.setFetchDirection(ResultSet.FETCH_FORWARD);

        PreparedStatement insertPs = connection.prepareStatement(insertSql);

        ResultSet resultSet = queryPS.executeQuery();
        while (resultSet.next()) {
            insertPs.setLong(1, resultSet.getLong("id"));
            insertPs.setInt(2, 1);
            insertPs.setDate(3, date1);
            insertPs.setInt(4, resultSet.getInt("period"));
            insertPs.setString(5, resultSet.getString("code"));
            insertPs.setString(6, resultSet.getString("busi_code"));
            insertPs.setLong(7, resultSet.getLong("dept_id"));
            insertPs.setLong(8, resultSet.getLong("user_id"));
            insertPs.setLong(9, resultSet.getLong("imputation_dept_id"));
            insertPs.setBigDecimal(10, resultSet.getBigDecimal("value"));
            insertPs.setLong(11, resultSet.getLong("source_dept"));
            insertPs.setLong(12, resultSet.getLong("zdys"));
            insertPs.setLong(13, resultSet.getLong("brks"));
            insertPs.setLong(14, resultSet.getLong("kzys"));
            insertPs.setString(15, resultSet.getString("mate_flag"));
            insertPs.setDate(16, resultSet.getDate("created_date"));
            insertPs.setLong(17, resultSet.getLong("tenant_id"));
            insertPs.setLong(18, resultSet.getLong("ward"));
            insertPs.addBatch();
            // 插入剩余的数量不足一个批次的数据
            if ((++i % insertBatchSize) == 0) {
                insertPs.executeBatch(); // 执行批量语句
                connection.commit();//所有语句都执行完毕后才手动提交sql语句
                insertPs.clearBatch(); // 清空批处理
                i = 0;
            }
        }
        if (i != 0) {
            insertPs.executeBatch(); // 执行批量语句
            connection.commit();//所有语句都执行完毕后才手动提交sql语句
            insertPs.clearBatch(); // 清空批处理
        }
        log.info("结束查询");
        int[] ints = insertPs.executeBatch();//   将一批命令提交给数据库来执行，如果全部命令执行成功，则返回更新计数组成的数组。
        //  如果数组长度不为0，则说明sql语句成功执行，即百万条数据添加成功！
        if (ints.length > 0) {
            log.info("成功添加数据");
        }
        log.info("结束插入");
        resultSet.close();
        queryPS.close();
        insertPs.close();
        connection.close();
    }


}
