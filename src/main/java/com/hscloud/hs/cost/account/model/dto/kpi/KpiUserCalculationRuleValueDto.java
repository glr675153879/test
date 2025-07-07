package com.hscloud.hs.cost.account.model.dto.kpi;

import lombok.Data;

/**
 * @Classname KpiUserCalculationRuleValueDto
 * @Description TODO
 * @Date 2025/1/6 16:59
 * @Created by sch
 */
@Data
public class KpiUserCalculationRuleValueDto {

    //"type": "custom",
    //		"value": 2000,
    //		"label": "病假",
    //		"code": "1663782469054357505"

    private String type;

    private String value;

    private String label;

    private String code;

}
