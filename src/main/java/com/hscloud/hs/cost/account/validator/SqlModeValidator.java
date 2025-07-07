package com.hscloud.hs.cost.account.validator;

import cn.hutool.core.util.StrUtil;
import com.hscloud.hs.cost.account.constant.enums.RoundEnum;
import com.hscloud.hs.cost.account.model.vo.ValidatorResultVo;
import com.hscloud.hs.cost.account.utils.SqlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Admin
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SqlModeValidator implements BaseValidator {

    private static final String TYPE = "SQL";


    private final SqlUtil sqlUtil;


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

        String sql = ruleConfig.getContent();

        // 获取 SQL 参数
        Map<String, String> params = ruleConfig.getParams();

        // 检查 SQL 是否为空
        if (StringUtils.isEmpty(sql)) {
            validatorResultVo.setErrorMsg("SQL 不能为空");
            return validatorResultVo;
        }

        // Check if SQL is a SELECT statement
        if (!isSelectSql(sql)) {
            validatorResultVo.setErrorMsg("不支持的 SQL 语句类型");
            return validatorResultVo;
        }

        try {
            long startTime = System.currentTimeMillis();
            String result = sqlUtil.executeSql(sql, params);

            validatorResultVo.setResult(dealRule(result, ruleConfig.getRetainDecimal(), ruleConfig.getCarryRule()));
            long endTime = System.currentTimeMillis();
            long executeTime = endTime - startTime;
            validatorResultVo.setExecuteTime((int) executeTime);

            return validatorResultVo;
        } catch (Exception e) {
            validatorResultVo.setErrorMsg("SQL 校验失败");
            log.error("SQL 校验失败", e);
            return validatorResultVo;
        }
    }

    private String dealRule(String result, Integer retainDecimal, String carryRule) {
        if (StrUtil.isBlank(result)) {
            return "0";
        }
        //转换成bigDecimal并根据保留小数位数进行进位规则处理
        BigDecimal bigDecimal = new BigDecimal(result);
        bigDecimal = bigDecimal.setScale(retainDecimal, RoundEnum.getCodeByDesc(carryRule));
        return bigDecimal.toString();
    }


    private String replaceSqlParameters(String sql, List<String> params) {
        for (int i = 0; i < params.size(); i++) {
            String paramPlaceholder = "#{" + i + "}";
            sql = sql.replace(paramPlaceholder, params.get(i));

            paramPlaceholder = "${" + i + "}";
            sql = sql.replace(paramPlaceholder, params.get(i));
        }
        sql = sql.replaceAll("\\s+", " ").trim();
        return sql;
    }

    private boolean isSelectSql(String sql) {
        sql = sql.replaceAll("\\s+", " ").trim();
        // Regular expression to match common SELECT statements
        String selectPattern = "^(?i)\\s*SELECT\\s+.+\\s+FROM\\s+.+$";
        return sql.matches(selectPattern);
    }


}
