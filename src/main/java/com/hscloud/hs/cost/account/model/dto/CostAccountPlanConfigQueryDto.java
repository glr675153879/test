package com.hscloud.hs.cost.account.model.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "查询 核算方案配置/方案配置的核算项 列表")
public class CostAccountPlanConfigQueryDto extends PageDto {
    @Schema(description = "方案id 查询核算方案配置列表")
    private Long planId;

    @Schema(description = "方案配置id 查询方案配置的核算项列表")
    private Long configId;
}
