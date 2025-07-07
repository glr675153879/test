package com.hscloud.hs.cost.account.utils;

import cn.hutool.core.collection.CollUtil;
import com.hscloud.hs.cost.account.model.dto.report.CustomParamDto;
import com.hscloud.hs.cost.account.model.dto.report.ParamDto;
import com.hscloud.hs.cost.account.model.dto.report.ReportDataDto;
import com.hscloud.hs.cost.account.model.vo.report.MetaDataBySqlVo;
import com.hscloud.hs.cost.account.model.vo.report.ReportTableDataVo;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nfunk.jep.JEP;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.stream.Collectors;


/**
 * JDBC工具类
 *
 * @author zyj
 * @date 2024/05/27
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JdbcUtil {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * 执行sql，通过结果集获取每个字段的类型及名称，别名
     */
    public List<MetaDataBySqlVo> getMetaDataBySql(String sql) {
        boolean ddlSql = SqlUtil.isDdlSql(sql);
        if (ddlSql) {
            throw new BizException("非法SQL");
        }
        String removeWhereSql = EnhanceSqlUtils.removeWhere(sql);
        List<MetaDataBySqlVo> metaDataBySqlVos = new ArrayList<>();
        ResultSetMetaData execute = jdbcTemplate.execute(removeWhereSql, PreparedStatement::getMetaData);
        // 将结果集的元数据信息封装到MetaDataBySqlVo中
        try {
            int columnCount = execute.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = execute.getColumnName(i);
                String columnTypeName = execute.getColumnTypeName(i);
                String columnLabel = execute.getColumnLabel(i);
                MetaDataBySqlVo vo = MetaDataBySqlVo.builder()
                        .sort(i).fieldName(columnLabel)
                        .fieldType(columnTypeName)
                        .fieldText(columnLabel)
                        .build();
                metaDataBySqlVos.add(vo);
            }
        } catch (Exception e) {
            log.error("获取元数据失败", e);
            throw new BizException("获取字段失败");
        }
        log.info("metaDataBySqlVos:{}", metaDataBySqlVos);
        return metaDataBySqlVos;
    }

    public Long count(String dbDynSql, ReportDataDto dto) {
        List<CustomParamDto> customParams = dto.getCustomParams();
        List<ParamDto> params = dto.getParams();
        String enhanceSql = EnhanceSqlUtils.enhanceCountSql(dbDynSql, customParams);
        log.info("countSql:{}", enhanceSql);
        Map<String, Object> paramMap = transferParam(customParams, params);
        return jdbcTemplate.queryForObject(enhanceSql, paramMap, Long.class);
    }

    public List<Map<String, Object>> queryPage(String dbDynSql, ReportDataDto dto, ReportTableDataVo resultVo) {
        List<CustomParamDto> customParams = dto.getCustomParams();
        List<ParamDto> params = dto.getParams();
        String enhanceSql = EnhanceSqlUtils.enhanceSql(dbDynSql, customParams, dto);
        log.info("queryPageSql:{}", enhanceSql);
        resultVo.setSql(enhanceSql);
        Map<String, Object> paramMap = transferParam(customParams, params);
        // 通过NamedParameterJdbcTemplate执行sql，返回List<Map<String, Object>>
        return jdbcTemplate.queryForList(enhanceSql, paramMap);
    }

    public List<Map<String, Object>> query(String dbDynSql, ReportDataDto dto, ReportTableDataVo resultVo) {
        List<CustomParamDto> customParams = dto.getCustomParams();
        List<ParamDto> params = dto.getParams();
        String enhanceSql = EnhanceSqlUtils.enhanceSql(dbDynSql, customParams);
        log.info("querySql:{}", enhanceSql);
        resultVo.setSql(enhanceSql);
        Map<String, Object> paramMap = transferParam(customParams, params);
        // 通过NamedParameterJdbcTemplate执行sql，返回List<Map<String, Object>>
        return jdbcTemplate.queryForList(enhanceSql, paramMap);
    }

    private Map<String, Object> transferParam(List<CustomParamDto> customParams, List<ParamDto> params) {
        Map<String, Object> paramMap = new HashMap<>();
        if (CollUtil.isNotEmpty(params)) {
            // 将params转为map
            paramMap.putAll(params.stream().filter(e -> Objects.nonNull(e.getCode()) && Objects.nonNull(e.getValue()))
                    .collect(Collectors.toMap(ParamDto::getCode, ParamDto::getValue, (key1, key2) -> key2)));
        }
        if (CollUtil.isNotEmpty(customParams)) {
            // 将params转为map
            paramMap.putAll(customParams.stream().filter(e -> Objects.nonNull(e.getCode()) && Objects.nonNull(e.getValue()))
                    .collect(Collectors.toMap(CustomParamDto::getCode, CustomParamDto::getValue, (key1, key2) -> key2)));
        }
        return paramMap;
    }

    public static void main(String[] args) {
        // 创建一个 JEP 对象
        JEP jep = new JEP();
        jep.addFunction("IF", new IfFunction());
        jep.parseExpression("IF(0,30,40)");
        // 计算结果
        BigDecimal resultBigDecimal = BigDecimal.valueOf(jep.getValue());
        // 打印AST
        System.out.println(resultBigDecimal);
    }

    @Slf4j
    static class IfFunction extends PostfixMathCommand {

        public IfFunction() {
            numberOfParameters = 3; // 可变参数
        }

        public void run(Stack inStack) throws ParseException {
            // 检查栈
            this.checkStack(inStack);
            Object falseValue = inStack.pop();
            Object trueValue = inStack.pop();
            Object condition = inStack.pop();
            inStack.push(this.ifCondition(condition, trueValue, falseValue));
        }

        public Object ifCondition(Object condition, Object trueValue, Object falseValue) throws ParseException {
            if (!(condition instanceof Number)) {
                throw new ParseException("Invalid parameter type");
            }
            if (!(trueValue instanceof Number)) {
                throw new ParseException("true expression parameter type");
            }
            if (!(falseValue instanceof Number)) {
                throw new ParseException("false expression parameter type");
            }
            log.info("condition:{},trueValue:{},falseValue:{}", condition, trueValue, falseValue);
            if (((Double) condition) == 0) {
                return trueValue;
            } else {
                return falseValue;
            }
        }

    }

}
