package com.hscloud.hs.cost.account.model.dto.userAttendance;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "科室单元dto")
public class AccountUnitDto {

    @Schema(description = "科室单元ID")
    private String id;

    @Schema(description = "科室单元名称")
    private String name;
}
