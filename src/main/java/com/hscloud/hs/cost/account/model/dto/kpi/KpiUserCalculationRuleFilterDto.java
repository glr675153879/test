package com.hscloud.hs.cost.account.model.dto.kpi;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Classname KpiUserCalculationRuleFilterDto
 * @Description TODO
 * @Date 2025/1/6 17:03
 * @Created by sch
 */
@Data
public class KpiUserCalculationRuleFilterDto {
    // "filter": [{
    //    "key": "gzxz",
    //    "value": [{
    //        "value": "外包",
    //        "label": "外包"
    //    }, {
    //        "value": "测试",
    //        "label": "测试"
    //    }]
    //  }],

    private String key;

    private List<KpiFormulaDto2.MemberListDTO> value;



}
