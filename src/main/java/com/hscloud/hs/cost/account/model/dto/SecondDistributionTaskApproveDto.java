package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 二次任务审核dto
 */
@Data
@Schema(description = "二次任务审核dto")
public class SecondDistributionTaskApproveDto {


    @Schema(description = "提交人员id")
    private Long userId;

    @Schema(description = "任务部门关联id")
    private Long taskDeptInfoId;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "工号")
    private String jobNumber;

    @Schema(description = "科室名称")
    private String deptName;

    @Schema(description = "科室id")
    private Long deptId;

    @Schema(description = "流程实例id")
    private Long processInstanceId;

    @Schema(description = "流程模板code")
    private String processCode;
}
