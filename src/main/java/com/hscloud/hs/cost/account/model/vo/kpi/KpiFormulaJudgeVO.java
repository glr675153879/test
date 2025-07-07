package com.hscloud.hs.cost.account.model.vo.kpi;

import com.hscloud.hs.cost.account.model.dto.DictDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class KpiFormulaJudgeVO {
    @Schema(description = "指标检验")
    private List<DictDto> indexJudge;
    @Schema(description = "方案校验")
    private List<DictDto> planJudge;
}
