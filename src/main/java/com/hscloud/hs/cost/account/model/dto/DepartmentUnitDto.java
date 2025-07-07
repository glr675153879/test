package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "科室单元dto")
public class DepartmentUnitDto {

    @Schema(description = "科室单元id")
    private Long departmentUnitId;

    @Schema(description = "科室单元名称")
    private String departmentUnitName;
}
