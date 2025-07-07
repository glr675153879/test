package com.hscloud.hs.cost.account.model.vo;


import com.hscloud.hs.cost.account.model.entity.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "任务详情Vo")
public class TaskDetailVo extends CostAccountTaskNew{

    @Schema(description = "核算单元列表")
    private List<CostAccountUnit> accountUnitList;

    @Schema(description = "核算人员列表")
    private List<DistributionUserInfo> accountUserList;

    @Schema(description = "核算指标列表")
    private List<CostAccountTaskConfigIndex> indexInfo;

    @Schema(description = "核算方案")
    private Long planId;

    @Schema(description = "核算对象类型")
    private String accountType;
}
