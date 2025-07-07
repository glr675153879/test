package com.hscloud.hs.cost.account.config;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author banana
 * @create 2024-09-23 23:06
 */

/**
 * 自定义 mybaits 拦截器
 */
public class MyCustomInterceptor implements InnerInterceptor {

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        String originalSql = boundSql.getSql();

        if (!ifAddType(originalSql))return;

        // 拼接条件
        String modifiedSql = addConditionToWhere(originalSql, "type = '0'");

        // 更新 BoundSql
        BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), modifiedSql, boundSql.getParameterMappings(), parameter);
        // 替换原有的 BoundSql
        Field field = null;
        try {
            field = boundSql.getClass().getDeclaredField("sql");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        field.setAccessible(true);
        try {
            field.set(boundSql, modifiedSql);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 是否需要添加隔离字段
     * @param sql
     * @return
     */
    private boolean ifAddType(String sql) {
        // 需要添加隔离字段的表
        return !ifBringType(sql)
                && (sql.contains("cost_report_item")
                || sql.contains("cost_report_detail_cost")
                || sql.contains("cost_report_detail_info")
                || sql.contains("cost_report_item_log")
                || sql.contains("cost_report_record")
                || sql.contains("cost_report_recprd_file_info")
                || sql.contains("cost_report_task")
                || sql.contains("cost_report_task_log"));
    }

    private static boolean ifBringType(String sql) {
        // 将 SQL 转为小写，统一处理大小写
        String lowerSql = sql.toLowerCase();

        // 查找 WHERE 子句的位置
        int whereIndex = lowerSql.indexOf("where");

        // 如果没有 WHERE 子句，返回 false
        if (whereIndex == -1) {
            return false;
        }

        // 截取 WHERE 后面的内容
        String whereClause = sql.substring(whereIndex + 5).trim();

        // 使用正则判断 WHERE 子句中是否包含独立的 'type' 字段（不允许是 'abc_type' 这种情况）
        // 这里假设 'type' 是一个独立的字段，可能后面跟着 '=', 'in' 等运算符
        String regex = "\\btype\\b(\\s*=?\\s*|\\s*in\\s*\\(.*\\))"; // 匹配 "type =" 或 "type in (...)"


        // 有id的也不匹配
        /*String regex2 = "\\bid\\b(\\s*=?\\s*|\\s*in\\s*\\(.*\\))";
        if(whereClause.matches("(?i).*" + regex2 + ".*")){return true;}*/


        return whereClause.matches("(?i).*" + regex + ".*");

    }

    /**
     * 添加隔离字段
     * @param sql
     * @param condition
     * @return
     */
    private String addConditionToWhere(String sql, String condition) {
        // 查找 WHERE 关键字的位置
        int whereIndex = sql.toUpperCase().indexOf("WHERE");
        if (whereIndex == -1) {
            // 如果没有 WHERE，直接添加
            return sql + " WHERE " + condition;
        } else {
            // 提取 WHERE 之后的部分
            String beforeWhere = sql.substring(0, whereIndex + 5); // 包含 "WHERE"
            String afterWhere = sql.substring(whereIndex + 5).trim();

            // 处理现有条件，确保是 AND 连接
            if (afterWhere.isEmpty()) {
                return beforeWhere + " " + condition;
            } else {
                return beforeWhere + " " + condition + " AND " + afterWhere;
            }
        }
    }
}