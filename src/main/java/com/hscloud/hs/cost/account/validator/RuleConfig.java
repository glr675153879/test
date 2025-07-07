package com.hscloud.hs.cost.account.validator;

import lombok.Data;

import java.util.Map;

/**
 * @author Admin
 */
@Data
public class RuleConfig {

    /**
     * 规则类型，如 SQL、公式、API
     */
    private String type;


    /**
     * 规则内容,可以是 SQL 语句或公式
     */
    private String content;


    /**
     * 校验方式，如 SQL 语法检查、执行结果校验
     */
    private String validationMethod;


    private Map<String,String> params;


    private String carryRule;

    private Integer retainDecimal;

}
