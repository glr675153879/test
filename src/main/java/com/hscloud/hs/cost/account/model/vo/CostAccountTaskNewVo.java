package com.hscloud.hs.cost.account.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hscloud.hs.cost.account.model.dto.CostAccountTaskNewDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountIndex;
import com.hscloud.hs.cost.account.model.entity.CostAccountPlan;
import com.hscloud.hs.cost.account.model.entity.CostAccountTaskNew;
import com.hscloud.hs.cost.account.model.entity.DistributionTaskGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 小小w
 * @date 2023/11/29 18:19
 */

@Data
@Schema(description = "核算任务信息")
public class CostAccountTaskNewVo {

    @Schema(description = "任务id")
    private CostAccountTaskNew costAccountTaskNew;

    @Schema(description = "核算任务分组")
    private List<CostAccountTaskNewVo.TaskGroupInfo> taskGroupInfoList;

    @Schema(description = "编辑到达层数")
    private String step;

    @Data
    @Schema(description = "关联分组参数")
    public static class TaskGroupInfo {

        @Schema(description = "主键id")
        private Long taskGroupInfoId;

        @Schema(description = "核算任务分组")
        private DistributionTaskGroup taskGroup;

        @Schema(description = "核算任务方案")
        private CostAccountPlan plan;

        @Schema(description = "核算对象id集合")
        private String accountObjectIds;

        @Schema(description = "指标信息")
        private List<CostAccountTaskNewVo.IndexInfo> indexInfoList;
    }

    @Data
    @Schema(description = "指标参数")
    public static class IndexInfo {
        @Schema(description = "指标配置id")
        private Long indexInfoId;

        @Schema(description = "指标")
        private CostAccountIndex index;

        @Schema(description = "是否关联 0:否  1:是")
        private String isRelevance;

        @Schema(description = "关联任务id")
        private Long relevanceTaskId;
    }

}
