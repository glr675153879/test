package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author banana
 * @create 2023-11-27 14:48
 */
@Data
public class SecondDistributionGetAccountIndexListDto {

    @Schema(description = "核算指标名称（模糊查询）")
    private String AccountIndexName;

    @Schema(description = "科室单元id")
    private Long unitId;

    @Schema(description = "方案配置表id")
    private Long planId;

}
