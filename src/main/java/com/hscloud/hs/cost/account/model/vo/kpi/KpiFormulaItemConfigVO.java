package com.hscloud.hs.cost.account.model.vo.kpi;

import lombok.Data;

import java.util.List;

@Data
public class KpiFormulaItemConfigVO {
    private String expression;
    private List<KpiFormulaItemConfigVO> list;
}
