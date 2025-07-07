package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.dto.DictDto;
import lombok.Data;

import java.util.List;

@Data
public class KpiFormulaDto {
    private String formulaOrigin;
    private String formulaShow;
    private List<KpiFormulaItemDto> fieldList;
    private List<DictDto> memberList;
    private List<KpiFormulaCondition> conditionList;
}
