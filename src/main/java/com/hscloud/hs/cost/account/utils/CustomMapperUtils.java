package com.hscloud.hs.cost.account.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 自定义映射器实用程序
 *
 * @author zyj
 * @date 2024/05/28
 */
@Slf4j
public class CustomMapperUtils {

    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

    // mybatis xml 文件起始字符串
    public static final String xmlStartSql = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n" +
            "<mapper namespace=\"com.hscloud.hs.cost.account.utils.CustomMapperUtils\">\n" +
            "    <select id=\"selectData\" resultType=\"java.util.Map\">";

    // mybatis xml 文件结束字符串
    public static final String xmlEndSql = "    </select>\n" +
            "</mapper>";

    // 查询语句ID
    public static final String statementSelectId = "selectData";

    public static void main(String[] args) {
        String selectSql = "SELECT\n" +
                "        *\n" +
                "        FROM\n" +
                "        cost_account_index\n" +
                "        <where>\n" +
                "            del_flag='0'\n" +
                "            <if test=\"name != null and name != ''\">\n" +
                "                <bind name=\"nameLike\" value=\"'%'+name+'%'\"/>\n" +
                "                AND name LIKE #{nameLike}\n" +
                "            </if>\n" +
                "            <if test=\"indexUnit != null and indexUnit != ''\">\n" +
                "                AND index_unit = #{indexUnit}\n" +
                "            </if>\n" +
                "            <if test=\"indexProperty != null and indexProperty != ''\">\n" +
                "                AND index_property = #{indexProperty}\n" +
                "            </if>\n" +
                "            <if test=\"statisticalCycle != null and statisticalCycle != ''\">\n" +
                "                AND statistical_cycle = #{statisticalCycle}\n" +
                "            </if>\n" +
                "            <if test=\"post != null and post.size()>0\">\n" +
                "                AND p.post_id in\n" +
                "                <foreach collection=\"post\" item=\"id\" open=\"(\" separator=\",\" close=\")\">\n" +
                "                    #{id}\n" +
                "                </foreach>\n" +
                "            </if>\n" +
                "            <if test=\"indexGroupId != null and indexGroupId != ''\">\n" +
                "                AND index_group_id = #{indexGroupId}\n" +
                "            </if>\n" +
                "            <if test=\"carryRule != null and carryRule != ''\">\n" +
                "                AND carry_rule = #{carryRule}\n" +
                "            </if>\n" +
                "            <if test=\"reservedDecimal != null\">\n" +
                "                AND reserved_decimal = #{reservedDecimal}\n" +
                "            </if>\n" +
                "            <if test=\"carryRule != null and carryRule != ''\">\n" +
                "                AND carry_rule = #{carryRule}\n" +
                "            </if>\n" +
                "            <if test=\"status != null and status != ''\">\n" +
                "                AND status = #{status}\n" +
                "            </if>\n" +
                "            <if test=\"isSystemIndex != null and isSystemIndex != ''\">\n" +
                "                AND is_system_index = #{isSystemIndex}\n" +
                "            </if>\n" +
                "\n" +
                "        </where>";
        HashMap paramMap = new HashMap();
        paramMap.put("name", "我爱罗");
        paramMap.put("indexUnit", "索引单元");
        paramMap.put("indexProperty", "索引属性");
        paramMap.put("statisticalCycle", "统计周期");
        paramMap.put("aastatisticalCycle1", "统计周期");
        paramMap.put("aastatisticalCycle12", "统计周期");
        paramMap.put("aastatisticalCycle144", "统计周期");
        ArrayList userIdList = new ArrayList();
        userIdList.add(1);
        userIdList.add(2);
        userIdList.add(3);
        paramMap.put("post", userIdList);
        String statementId = "selectData";
        String sql = parseMybatisSelectSql(selectSql, paramMap);
        log.info("获取到的:statementId={}语句的最终执行sql:{}", statementId, sql);
    }


    /**
     * @Description 解析含有mybatis 标签的查询sql,只需要传sql过来就行,方法自动拼接前后mybatis xml文件前后字符
     * @Author chengweiping
     * @Date 2021/6/10 9:37
     */
    public static String parseMybatisSelectSql(String selectSql, Map paramMap) {
        log.info("原始查询sql:{}", selectSql);
        String sourceSql = xmlStartSql + selectSql + xmlEndSql;
        log.info("原始mybatis xml sql:{}", sourceSql);
        //解析
        Configuration configuration = new Configuration();
        XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(new ReaderInputStream(new StringReader(sourceSql), "UTF-8"), configuration, sourceSql, new HashMap<String, XNode>());
        xmlMapperBuilder.parse();
        MappedStatement mappedStatement = xmlMapperBuilder.getConfiguration().getMappedStatement(statementSelectId);
        BoundSql boundSql = mappedStatement.getBoundSql(paramMap);
        String compileSql = boundSql.getSql();
        log.info("去掉mybatis标签后compileSql:{}", compileSql);
//        return compileSql;
        //替换参数值获取最终的sql
        String finalSql = replaceSqlParamValue(configuration, boundSql);
        log.info("替换参数值后finalSql:{}", finalSql);
        return finalSql;
    }


    /**
     * @Description 解析含有mybatis 标签的sql (包含完整的xml文件字符串）
     * @Author chengweiping
     * @Date 2021/6/10 9:37
     */
    public static String parseMybatisSql(String statementId, String sourceSql, Map paramMap) {
        //解析
        Configuration configuration = new Configuration();
        XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(new ReaderInputStream(new StringReader(sourceSql), "UTF-8"), configuration, sourceSql, new HashMap<String, XNode>());
        xmlMapperBuilder.parse();
        MappedStatement mappedStatement = xmlMapperBuilder.getConfiguration().getMappedStatement(statementId);
        BoundSql boundSql = mappedStatement.getBoundSql(paramMap);
        String compileSql = boundSql.getSql();
        log.info("去掉mybatis标签后compileSql:{}", compileSql);
        //替换参数值获取最终的sql
        String finalSql = replaceSqlParamValue(configuration, boundSql);
        log.info("替换参数值后finalSql:{}", finalSql);
        return finalSql;
    }

    /**
     * @Description 替换sql参数值
     * @Author chengweiping
     * @Date 2021/6/10 9:37
     */
    private static String replaceSqlParamValue(Configuration configuration, BoundSql boundSql) {
        String sql = boundSql.getSql();
        //美化Sql
        sql.replaceAll("[\\s\n]+", " ");
        // 填充占位符， 把传参填进去，使用#｛｝、${} 一样的方式
        Object parameterObject = boundSql.getParameterObject();
        //参数映射列表，有入的（in） 有出的（O），后面只遍历传入的参数，并且把传入的参数赋值成我们代码里的值
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();

        ArrayList<String> parmeters = new ArrayList<String>();
        if (parameterMappings != null) {
            MetaObject metaObject = parameterObject == null ? null : configuration.newMetaObject(parameterObject);
            for (int i = 0; i < parameterMappings.size(); i++) {
                ParameterMapping parameterMapping = parameterMappings.get(i);
                if (parameterMapping.getMode() != ParameterMode.OUT) {
                    parmeters.add("#{" + parameterMapping.getProperty() + "}");
//                    //参数值
//                    Object value;
//                    String propertyName = parameterMapping.getProperty();
//                    //获取参数名称
//                    if (boundSql.hasAdditionalParameter(propertyName)) {
//                        //获取参数值
//                        value = boundSql.getAdditionalParameter(propertyName);
//                    } else if (parameterObject == null) {
//                        value = null;
//                    } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
//                        //如果是单个值则直接赋值
//                        value = parameterObject;
//                    } else {
//                        value = metaObject == null ? null : metaObject.getValue(propertyName);
//                    }
//                    //由于传参类型多种多样，数值型的直接把value的值传参进去即可，如果是字符串的、日期的，要在前后加上单引号‘’，才能到sql里判读语句里使用
//                    if (value instanceof Number) {
//                        parmeters.add(String.valueOf(value));
//                    } else {
//                        StringBuilder builder = new StringBuilder();
//                        builder.append("'");
//                        if (value instanceof Date) {
//                            builder.append(simpleDateFormat.format((Date) value));
//                        } else if (value instanceof String) {
//                            builder.append(value);
//                        }
//                        builder.append("'");
//                        parmeters.add(builder.toString());
//                    }
                }
            }
        }
        //sql里的东西处理完了，都返回给我们声明list容器里了，接下来要用我们一开始获取到绑定的sql 来替换赋值，把真正 传参赋值的sql返回给计算引擎，spark或者flink
        for (String value : parmeters) {
            /*
             把占位符问号 逐个 从第一位去替换我们list里获取到的值。如果对ArrayList数组的顺序不放心，可以换成LinkList去实现，不需要考虑性能问题
             毕竟最多10来个参数，如果你写sql还要传入上千个的参数，那么就得好好反思，是不是要改成代码去实现了。
             */
            sql = sql.replaceFirst("\\?", value);
        }
        log.info("parmeters:{}", JSON.toJSONString(parmeters));
        return sql;

    }
}