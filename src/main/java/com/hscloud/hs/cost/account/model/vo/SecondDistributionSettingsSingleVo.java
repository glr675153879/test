package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "二次分配设置单项vo")
public class SecondDistributionSettingsSingleVo {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "新性质")
    private String newProperty;

    @Schema(description = "说明")
    private String description;

    @Schema(description = "状态  0 启用  1 停用")
    private String status;
}
