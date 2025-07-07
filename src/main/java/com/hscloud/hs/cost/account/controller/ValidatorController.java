package com.hscloud.hs.cost.account.controller;

import cn.hutool.core.date.DateUtil;
import com.google.common.collect.Maps;
import com.hscloud.hs.cost.account.constant.enums.FieldEnum;
import com.hscloud.hs.cost.account.model.dto.SqlValidatorDto;
import com.hscloud.hs.cost.account.model.vo.ValidatorResultVo;
import com.hscloud.hs.cost.account.validator.RuleConfig;
import com.hscloud.hs.cost.account.validator.ValidatorHolder;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Admin
 */
@RestController
@Schema(description = "校验器")
@RequiredArgsConstructor
@Tag(description = "validator", name = "校验器")
public class ValidatorController {

    private final ValidatorHolder validatorHolder;


    /**
     * sql配置校验
     */
    @PostMapping("/sql/config/validator")
    public R<ValidatorResultVo> sqlConfigValidator(@RequestBody @Validated SqlValidatorDto sqlValidatorDto) {
        RuleConfig ruleConfig = new RuleConfig();
        ruleConfig.setContent(sqlValidatorDto.getSql());
        Map<String, String> params = Maps.newHashMap();
        sqlValidatorDto.getParams().forEach(param ->
        {

            if (FieldEnum.DATE.getCode().equals(param.getType())) {
                //todo 日期格式校验
                params.put(param.getKey(),DateFormatUtils.format(DateUtil.parse(param.getValue()), "yyyyMM"));
            }else {
                params.put(param.getKey(),param.getValue());
            }
        });
        ruleConfig.setParams(params);
        ruleConfig.setType(sqlValidatorDto.getType());
        ruleConfig.setCarryRule(sqlValidatorDto.getCarryRule());
        ruleConfig.setRetainDecimal(sqlValidatorDto.getRetainDecimal());
        return R.ok(validatorHolder.getValidatorByType(sqlValidatorDto.getType()).validate(ruleConfig));
    }

}
