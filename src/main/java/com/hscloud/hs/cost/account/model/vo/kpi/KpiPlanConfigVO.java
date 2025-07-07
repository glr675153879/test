package com.hscloud.hs.cost.account.model.vo.kpi;

import com.hscloud.hs.cost.account.model.dto.DictDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiFormulaCondition;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiFormulaItemDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class KpiPlanConfigVO {
//    @Schema(description = "指标名称")
//    private String itemName;
    @Schema(defaultValue = "公式id")
    private Long formulaId;

    private String formulaOrigin;
    private String formulaShow;
    private List<KpiFormulaItemVO> fieldList;
    private List<DictDto> memberList;
    private List<KpiFormulaCondition> conditionList;
    private String delFlag;
    private String status;
    private String busiType;
    @Data
    public static class ItemPool{
        @Schema(description = "index && item")
        private String type;
        private Set<String> code;
    }
}
