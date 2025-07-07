package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class KpiItemCoefficientDTO {

    @Schema(description = "科室id")
    @NotNull(message = "科室id不能为空")
    private Long accountUnitId;

    @Schema(description = "周期")
    @NotNull(message = "周期不能为空")
    private Long period;

    @Schema(description = "核算项ids")
    @NotNull(message = "核算项ids不能为空")
    private List<Long> itemIds;

    @Schema(description = "是否继承 0-否 1-是")
    @NotNull(message = "是否继承不能为空")
    private String inheritFlag;

    @Schema(description = "系数map(userId, 系数)")
    @NotNull(message = "系数map不能为空")
    private Map<Long, BigDecimal> coefficientMap;

    @Schema(description = "修改者类型 0-科室 1-绩效办")
    @NotNull(message = "修改者类型不能为空")
    private String changeFlag;
}
