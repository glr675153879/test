package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "当量分配方式dto")
public class KpiItemEquivalentDistributeDTO {

    @Schema(description = "核算项id")
    @NotNull(message = "核算项id不能为空")
    private Long itemId;

    @Schema(description = "周期")
    @NotNull(message = "周期不能为空")
    private Long period;

    @Schema(description = "科室id")
    @NotNull(message = "科室id不能为空")
    private Long accountUnitId;

    @Schema(description = "分配方式 0-平均分配，1-系数分配，2-自定义分配")
    @NotNull(message = "分配方式不能为空")
    private String distributeType;

    @Schema(description = "类型 0-科室 1-绩效办")
    @NotNull(message = "修改类型不能为空")
    private String changeFlag;

    @Schema(description = "系数map(人员id, 系数)")
    Map<Long, BigDecimal> coefficientMap;

    @Schema(description = "人员当量数据，自定义分配使用")
    List<KpiItemEquivalentChangeDTO> changeRecords;
}
