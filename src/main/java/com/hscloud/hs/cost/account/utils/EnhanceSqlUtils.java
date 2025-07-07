package com.hscloud.hs.cost.account.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.util.JdbcUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.constant.enums.report.OperatorEnum;
import com.hscloud.hs.cost.account.model.dto.report.CustomParamDto;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * sql增强工具类
 *
 * @author : [pc]
 * @version : [v1.0]
 * @createTime : [2024/4/29 15:53]
 */
@Slf4j
public class EnhanceSqlUtils {

    /**
     * 增强sql
     * 根据入参，拼接sql条件，然后生成count接口
     *
     * @param sql        sql
     * @param conditions 条件
     * @return 增强后的sql
     */
    public static String enhanceCountSql(@NotNull String sql, List<CustomParamDto> conditions) {
        String enhanceWhereSql = enhanceSql(sql, conditions);
        return "select count(*) from (" + enhanceWhereSql + ") as t ";
    }

    /**
     * 增强sql
     * 根据入参，拼接where条件，拼接分页参数
     *
     * @param sql        sql
     * @param conditions 条件
     * @param page       分页
     * @return 增强后的sql
     */
    public static String enhanceSql(@NotNull String sql, List<CustomParamDto> conditions, Page<?> page) {
        StringBuilder constraintsBuffer = new StringBuilder(" 1=1 ");
        if (CollUtil.isNotEmpty(conditions)) {
            for (CustomParamDto entry : conditions) {
                if (Objects.isNull(entry.getValue())) {
                    continue;
                }
                // like, =, !=, >, <, >=, <=, in
                if (StrUtil.equals(entry.getOperator(), OperatorEnum.IN.getOperator())) {
                    constraintsBuffer.append(StrUtil.format(" and t.{} in (:{}) ", entry.getCode(), entry.getCode()));
                } else if (StrUtil.equals(entry.getOperator(), OperatorEnum.EQ.getOperator())) {
                    constraintsBuffer.append(StrUtil.format(" and t.{} = :{} ", entry.getCode(), entry.getCode()));
                } else if (StrUtil.equals(entry.getOperator(), OperatorEnum.NE.getOperator())) {
                    constraintsBuffer.append(StrUtil.format(" and t.{} != :{} ", entry.getCode(), entry.getCode()));
                } else if (StrUtil.equals(entry.getOperator(), OperatorEnum.GT.getOperator())) {
                    constraintsBuffer.append(StrUtil.format(" and t.{} > :{} ", entry.getCode(), entry.getCode()));
                } else if (StrUtil.equals(entry.getOperator(), OperatorEnum.LT.getOperator())) {
                    constraintsBuffer.append(StrUtil.format(" and t.{} < :{} ", entry.getCode(), entry.getCode()));
                } else if (StrUtil.equals(entry.getOperator(), OperatorEnum.GE.getOperator())) {
                    constraintsBuffer.append(StrUtil.format(" and t.{} >= :{} ", entry.getCode(), entry.getCode()));
                } else if (StrUtil.equals(entry.getOperator(), OperatorEnum.LE.getOperator())) {
                    constraintsBuffer.append(StrUtil.format(" and t.{} <= :{} ", entry.getCode(), entry.getCode()));
                } else {
                    // 默认为like
                    constraintsBuffer.append(StrUtil.format(" and t.{} like CONCAT('%', :{}, '%') ", entry.getCode(), entry.getCode()));
                }

            }
        }
        String enhanceWhereSql = "select * from (" + sql + ") as t where " + constraintsBuffer;

        if (Objects.isNull(page)) {
            return enhanceWhereSql;
        }
        return enhanceWhereSql + " limit " + (page.getSize() * (page.getCurrent() - 1)) + " , " + page.getSize();
    }

    /**
     * 增强sql
     * 根据入参，拼接where条件
     *
     * @param sql        sql
     * @param conditions 条件
     * @return 增强后的sql
     */
    public static String enhanceSql(@NotNull String sql, List<CustomParamDto> conditions) {
        return enhanceSql(sql, conditions, null);
    }

    public static String removeWhere(String sql) {
        log.info("enhanced removeWhere before sql: {}", sql);
        // SQLParserUtils.createSQLStatementParser可以将sql装载到Parser里面
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcUtils.MYSQL);
        // parseStatementList的返回值SQLStatement本身就是druid里面的语法树对象
        List<SQLStatement> stmtList = parser.parseStatementList();
        SQLStatement stmt = stmtList.get(0);
        if (!(stmt instanceof SQLSelectStatement)) {
            throw new BizException("not select statement");
        }
        SQLSelectStatement selectStmt = (SQLSelectStatement) stmt;
        SQLSelect sqlselect = selectStmt.getSelect();
        removeWhere(sqlselect);
        log.info("enhanced removeWhere after sql: {}", sqlselect);
        return sqlselect.toString();
    }

    private static void removeWhere(SQLSelect sqlselect) {
        SQLSelectQueryBlock query = (SQLSelectQueryBlock) sqlselect.getQuery();
        // 移除where表达式
        query.setWhere(null);
        SQLTableSource from = ((SQLSelectQueryBlock) sqlselect.getQuery()).getFrom();
        if (from instanceof SQLSubqueryTableSource) {
            removeWhere(((SQLSubqueryTableSource) from).getSelect());
        } else if (from instanceof SQLJoinTableSource) {
            SQLJoinTableSource from1 = (SQLJoinTableSource) from;
            SQLTableSource left = from1.getLeft();
            if (left instanceof SQLSubqueryTableSource) {
                removeWhere(((SQLSubqueryTableSource) left).getSelect());
            }
            SQLTableSource right = from1.getRight();
            if (right instanceof SQLSubqueryTableSource) {
                removeWhere(((SQLSubqueryTableSource) right).getSelect());
            }
        }
    }

    /**
     * Replace placeholders in SQL query with actual values using regular expressions.
     *
     * @param sql The original SQL query.
     * @return The SQL query with placeholders replaced by actual values.
     */
    public static String replaceWhere(String sql) {
        log.info("enhanced replaceWhere before sql: {}", sql);
        // Replace #{xxx} placeholders
        Pattern pattern1 = Pattern.compile("#\\{(.*?)}");
        Matcher matcher1 = pattern1.matcher(sql);
        StringBuffer sb1 = new StringBuffer();
        while (matcher1.find()) {
            matcher1.appendReplacement(sb1, ":" + matcher1.group(1));
        }
        matcher1.appendTail(sb1);
        sql = sb1.toString();

        // Replace ${xxx} placeholders
        Pattern pattern2 = Pattern.compile("\\$\\{(.*?)}");
        Matcher matcher2 = pattern2.matcher(sql);
        StringBuffer sb2 = new StringBuffer();
        while (matcher2.find()) {
            matcher2.appendReplacement(sb2, ":" + matcher2.group(1));
        }
        matcher2.appendTail(sb2);
        log.info("enhanced replaceWhere after sql: {}", sb2);
        return sb2.toString();
    }

}
