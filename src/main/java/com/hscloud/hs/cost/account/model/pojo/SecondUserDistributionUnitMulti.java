package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class SecondUserDistributionUnitMulti {

    @Schema(description = "management管理绩效,workload工作量 single单项")
    private String type;

    @Schema(description = "核算指标id")
    private Long indexId;

    @Schema(description = "核算指标名称")
    private String indexName;

    @Schema(description = "核算方案id")
    private Long planId;

    private List<SecondUserDistributionSmallUnit> secondUserDistributionSmallUnitList;
}
