package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Classname KpiCoefficientDto
 * @Description TODO
 * @Date 2024/11/27 13:41
 * @Created by sch
 */
@Data
public class KpiCoefficientDto {

    @Schema(description = "字典类型")
    private String dic_type;

    @Schema(description = "字典值")
    private String dic_code;

    @Schema(description = "修改系数")
    private BigDecimal value;
}
