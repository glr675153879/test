package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
@Data
@Schema(description = "二次分配设置职称vo")
public class SecondDistributionSettingsRankVo {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "职称级别")
    private String name;

    @Schema(description = "系数")
    private BigDecimal coefficient;

    @Schema(description = "状态  0 启用  1 停用")
    private String status;
}
