package com.hscloud.hs.cost.account.model.vo.kpi;

import lombok.Data;

import java.util.List;

@Data
public class KpiPlanIndexConfigInfoVO {
    private String expression;
    private List<KpiFormulaItemConfigVO> 表达式项List;
}
