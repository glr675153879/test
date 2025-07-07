package com.hscloud.hs.cost.account.model.vo;

import com.hscloud.hs.cost.account.model.pojo.MonthRatio;
import com.hscloud.hs.cost.account.model.pojo.YearRatio;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "核算单元下的指标vo")
public class CostUnitIndexVo {

    @Schema(description = "核算任务id")
    private Long taskId;

    @Schema(description = "核算单元id")
    private Long unitId;

    @Schema(description = "指标总核算值")
    private BigDecimal totalCount=BigDecimal.ZERO;

    @Schema(description = "所占百分比")
    private BigDecimal percentage=BigDecimal.ZERO;

    @Schema(description = "核算指标id")
    private Long indexId;

    @Schema(description = "核算指标名")
    private String indexName;

    @Schema(description = "父级核算指标id")
    private Long parentId;

    @Schema(description = "路径")
    private String path;

    @Schema(description = "核算项")
    private List<CostUnitItemVo> costUnitItemVoList;
}
