package com.hscloud.hs.cost.account.model.dto.kpi;

import lombok.Data;

import java.util.List;

/**
 * @Classname KpiUserCalculationRuleFilterDto
 * @Description TODO
 * @Date 2025/1/6 17:03
 * @Created by sch
 */
@Data
public class KpiUserCalculationRuleRangeDto {
    // "range": {
    //      "paramType": "15",
    //      "paramValues": [{
    //          "value": "外包",
    //          "label": "外包"
    //      }, {
    //          "value": "测试",
    //          "label": "测试"
    //      }]
    //  },

    private String paramType;

    private List<KpiFormulaDto2.MemberListDTO> paramValues;

    private List<KpiFormulaDto2.MemberListDTO> paramExcludes;



}
