package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "当量配置复制")
public class KpiItemEquivalentConfigCopyDTO {

    @Schema(description = "当量配置id")
    List<Long> configIds;

    @Schema(description = "目标科室id列表")
    List<Long> targetAccountUnitIds;
}
