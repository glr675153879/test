package com.hscloud.hs.cost.account.model.vo;

import com.hscloud.hs.oa.workflow.api.vo.FlwInstanceTaskVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 返回二次审批vo
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SecondDistributionTaskApproveVo extends FlwInstanceTaskVo {

    @Schema(description = "主键id")
    private Long taskUnitId;

    @Schema(description = "分配任务名称")
    private String taskName;

    @Schema(description = "任务类型")
    private String taskType;

    @Schema(description = "分配周期")
    private String taskPeriod;

    @Schema(description = "发放单元名称")
    private String deptName;

    @Schema(description = "提交人")
    private String submitName;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "状态")
    private String status;

}
