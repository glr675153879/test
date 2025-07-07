package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author 小小w
 * @date 2023/12/1 10:38
 */

@Data
@Schema(description = "核算任务计算过程Dto(新)")
public class CostAccountTaskCalculateProcessNewDto {

    @Schema(description = "核算明细名称")
    private String accountTaskName;

    @Schema(description = "核算时间")
    private String accountTime;

    @Schema(description = "指标项名称(住院收入/日间)/成本绩效只有总核算值传这个，指标的下钻只传indexId")
    private String indexName;

    @Schema(description = "核算任务id")
    private Long accountTaskId;

    @Schema(description = "核算任务分组id")
    private Long accountTaskGroupId;

    @Schema(description = "成本绩效查询下钻指标id")
    private Long indexId;

    @Schema(description = "科室code")
    private Long unitId;

    @Schema(description = "人员id")
    private Long userId;

}
