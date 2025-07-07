package com.hscloud.hs.cost.account.model.dto;


import com.hscloud.hs.oa.workflow.api.dto.ProcessInstanceCreateDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务提交Dto
 */
@Data
@Schema(description = "任务提交传输对象")
@EqualsAndHashCode(callSuper = true)
public class SecondDistributionTaskSubmitDto extends ProcessInstanceCreateDto {

    @Schema(description = "任务科室单元关联id")
    private Long taskUnitId;

    @Schema(description = "方案id")
    private Long planId;

    @Schema(description = "分配任务名称")
    private String taskName;

    @Schema(description = "任务类型")
    private String taskType;

    @Schema(description = "分配周期")
    private String taskPeriod;
}
