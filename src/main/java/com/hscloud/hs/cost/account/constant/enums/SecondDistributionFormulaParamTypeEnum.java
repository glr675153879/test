package com.hscloud.hs.cost.account.constant.enums;

import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author banana
 * @create 2023-11-28 14:29
 */
@Getter
public enum SecondDistributionFormulaParamTypeEnum {

    HSZB(0, "核算指标参数"),
    ZGS(1,"总公式参数");

    private Integer type;

    private String desc;

    SecondDistributionFormulaParamTypeEnum(Integer type, String desc){
        this.type = type;
        this.desc = desc;
    }
}
