package com.hscloud.hs.cost.account.model.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分摊科室单元id和名称dto")
public class AccountUnitIdAndNameDto {

    @Schema(description = "科室单元ID")
    private Long accountUnitId;

    @Schema(description = "科室单元名称")
    private String accountUnitName;
}
