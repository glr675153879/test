package com.hscloud.hs.cost.account.model.vo.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
public class KpiItemEquivalentTaskVO {

    private Long id;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "任务状态 -1:驳回 0:未提交 10:待审核 20:通过")
    private String status;

    @Schema(description = "周期")
    private Long period;

    @Schema(description = "科室id")
    private Long accountUnitId;

    @Schema(description = "科室名称")
    private String accountUnitName;

    @Schema(description = "科室责任人")
    private String responsiblePersonName;

    @Schema(description = "下发时间")
    private Date createdDate;

    @Schema(description = "提交时间")
    private Date committedDate;

    @Schema(description = "驳回原因")
    private String reason;

    @Schema(description = "自动重新下发 0-否 1-是")
    private String autoIssue;
}
