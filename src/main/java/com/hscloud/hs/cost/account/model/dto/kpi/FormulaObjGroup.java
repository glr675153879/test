package com.hscloud.hs.cost.account.model.dto.kpi;

import lombok.Data;

import java.util.List;

@Data
public class FormulaObjGroup {
    private String indexCode;
    private Long userId;
    private List<Long> planObj;
    public FormulaObjGroup(String indexCode,Long userId) {
        this.indexCode = indexCode;
        this.userId = userId;
    }
    public FormulaObjGroup(String indexCode, List<Long> planObj) {
        this.indexCode = indexCode;
        this.planObj = planObj;
    }
}
