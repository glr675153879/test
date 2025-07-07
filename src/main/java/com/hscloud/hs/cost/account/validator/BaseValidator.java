package com.hscloud.hs.cost.account.validator;

import com.hscloud.hs.cost.account.model.vo.ValidatorResultVo;

/**
 * @author Admin
 */
public interface BaseValidator {

    /**
     * 获取校验器类型
     * @return 校验器类型
     */
    String getType();

    /**
     * 校验配置是否正确
     * @param ruleConfig 校验配置
     * @return 校验结果
     */
    ValidatorResultVo validate(RuleConfig ruleConfig);
}
