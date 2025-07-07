package com.hscloud.hs.cost.account.validator;

import com.alibaba.fastjson.JSON;
import com.hscloud.hs.cost.account.constant.enums.report.OperatorEnum;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemCondDto;
import com.hscloud.hs.cost.account.model.vo.ValidatorResultVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqlCondValidator implements BaseValidator {
    private static final String TYPE = "SQL_COND";

    // 字段类型与支持的操作符映射
    private static final Map<String, List<String>> FIELD_TYPE_OPERATORS = new HashMap<>();

    static {
        FIELD_TYPE_OPERATORS.put("varchar", Arrays.asList("=", "!=", "<>", "like", "in", "not like", "not in", "is null", "is not null"));
        FIELD_TYPE_OPERATORS.put("char", Arrays.asList("=", "!=", "<>", "like", "in", "not like", "not in", "is null", "is not null"));
        FIELD_TYPE_OPERATORS.put("text", Arrays.asList("=", "!=", "<>", "like", "in", "not like", "not in", "is null", "is not null"));
        FIELD_TYPE_OPERATORS.put("int", Arrays.asList("=", "!=", "<>", ">", "<", ">=", "<=", "in", "not in", "is null", "is not null"));
        FIELD_TYPE_OPERATORS.put("bigint", Arrays.asList("=", "!=", "<>", ">", "<", ">=", "<=", "in", "not in", "is null", "is not null"));
        FIELD_TYPE_OPERATORS.put("smallint", Arrays.asList("=", "!=", "<>", ">", "<", ">=", "<=", "in", "not in", "is null", "is not null"));
        FIELD_TYPE_OPERATORS.put("tinyint", Arrays.asList("=", "!=", "<>", ">", "<", ">=", "<=", "in", "not in", "is null", "is not null"));
        FIELD_TYPE_OPERATORS.put("mediumint", Arrays.asList("=", "!=", "<>", ">", "<", ">=", "<=", "in", "not in", "is null", "is not null"));
        FIELD_TYPE_OPERATORS.put("float", Arrays.asList("=", "!=", "<>", ">", "<", ">=", "<=", "in", "not in", "is null", "is not null"));
        FIELD_TYPE_OPERATORS.put("double", Arrays.asList("=", "!=", "<>", ">", "<", ">=", "<=", "in", "not in", "is null", "is not null"));
        FIELD_TYPE_OPERATORS.put("decimal", Arrays.asList("=", "!=", "<>", ">", "<", ">=", "<=", "in", "not in", "is null", "is not null"));
        FIELD_TYPE_OPERATORS.put("datetime", Arrays.asList("=", "!=", "<>", ">", "<", ">=", "<=", "is null", "is not null"));
        FIELD_TYPE_OPERATORS.put("timestamp", Arrays.asList("=", "!=", "<>", ">", "<", ">=", "<=", "is null", "is not null"));
        FIELD_TYPE_OPERATORS.put("date", Arrays.asList("=", "!=", "<>", ">", "<", ">=", "<=", "is null", "is not null"));
        FIELD_TYPE_OPERATORS.put("time", Arrays.asList("=", "!=", "<>", ">", "<", ">=", "<=", "is null", "is not null"));
        FIELD_TYPE_OPERATORS.put("year", Arrays.asList("=", "!=", "<>", ">", "<", ">=", "<=", "is null", "is not null"));
        FIELD_TYPE_OPERATORS.put("json", Arrays.asList("=", "!=", "<>", "is null", "is not null"));
        FIELD_TYPE_OPERATORS.put("blob", Arrays.asList("is null", "is not null"));
        FIELD_TYPE_OPERATORS.put("binary", Arrays.asList("is null", "is not null"));
        FIELD_TYPE_OPERATORS.put("varbinary", Arrays.asList("is null", "is not null"));
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public ValidatorResultVo validate(RuleConfig ruleConfig) {
        ValidatorResultVo validatorResultVo = new ValidatorResultVo();
        if (!TYPE.equalsIgnoreCase(ruleConfig.getType())) {
            validatorResultVo.setErrorMsg("不支持的校验类型");
            return validatorResultVo;
        }

        String content = ruleConfig.getContent();
        List<KpiItemCondDto> condList = JSON.parseArray(content, KpiItemCondDto.class);

        long startTime = System.currentTimeMillis();
        for (KpiItemCondDto condDto : condList) {
            if ("group".equals(condDto.getType())) {
                List<KpiItemCondDto> data = condDto.getData();
                if (!CollectionUtils.isEmpty(data)) {
                    RuleConfig config = new RuleConfig();
                    config.setType("SQL_COND");
                    config.setContent(JSON.toJSONString(data));
                    validate(config);
                }
                continue;
            }
            String operator = condDto.getOperator();
            String fieldType = condDto.getFieldType();
            String fieldValue = condDto.getFieldValue();
            String fieldName = condDto.getFieldName();

            if (operator == null || operator.isEmpty()) {
                validatorResultVo.setErrorMsg(fieldName + ":运算符不能为空");
                break;
            }

            if (fieldType == null || fieldType.isEmpty()) {
                validatorResultVo.setErrorMsg(fieldName + ":字段类型不能为空");
                break;
            }

            // 检查字段类型是否支持该操作符
            List<String> validOperators = FIELD_TYPE_OPERATORS.get(fieldType);
            if (validOperators == null || !validOperators.contains(operator)) {
                validatorResultVo.setErrorMsg(fieldName + ":不支持的字段操作类型");
                break;
            }

            // 检查字段值是否符合要求
            if (OperatorEnum.IN.getOperator().equals(operator) || OperatorEnum.NOT_IN.getOperator().equals(operator)) {
                String[] values = fieldValue.split(",");
                for (String value : values) {
                    if (!value.matches("[^,]+")) {
                        validatorResultVo.setErrorMsg(fieldName + ":in 或not in操作符的字段值只能包含英文逗号分隔的值");
                        break;
                    }
                }
            } else if (OperatorEnum.LIKE.getOperator().equals(operator) || OperatorEnum.NOT_LIKE.getOperator().equals(operator)) {
                if (!fieldValue.startsWith("'%") && !fieldValue.endsWith("%'")) {
                    validatorResultVo.setErrorMsg(fieldName + ":like或not like操作符的字段值必须以%开头或结尾");
                    break;
                }
            }
        }
        long endTime = System.currentTimeMillis();
        long executeTime = endTime - startTime;
        validatorResultVo.setExecuteTime((int) executeTime);

        return validatorResultVo;
    }
}
