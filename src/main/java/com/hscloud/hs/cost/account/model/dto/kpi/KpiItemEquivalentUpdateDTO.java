package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
public class KpiItemEquivalentUpdateDTO {

    @NotNull(message = "id不能为空")
    private Long id;

    @Schema(description = "总工作量")
    private BigDecimal newTotalWorkload;

    @Schema(description = "分配方式 0-平均分配，1-系数分配，2-自定义分配")
    private String distributeType;

    @Schema(description = "系数")
    private BigDecimal coefficient;

    @Schema(description = "人员当量列表")
    List<KpiItemEquivalentUpdateDTO> childDTOList;
}
