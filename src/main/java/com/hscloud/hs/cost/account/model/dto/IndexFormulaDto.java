package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "指标公式dto")
public class IndexFormulaDto {

    //配置项
    @Schema(description = "配置项名称")
    private String configName;

    //核算对象
    @Schema(description = "核算对象名称")
    private String accountObjectName;

    //科室单元范围
    @Schema(description = "科室单元范围名称")
    private String departmentUnitScopeName;

    //科室单元
    @Schema(description = "科室单元")
    private List<AccountUnitIdAndNameDto> accountUnit;

    //核算科室单元科室性质
    @Schema(description = "核算科室单元科室性质名称")
    private String natureOfDepartmentName;

    //核算周期名称
    @Schema(description = "核算周期名称")
    private String accountCycleName;
}
