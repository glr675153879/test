package com.hscloud.hs.cost.account.model.dto.dataReport;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "上报管理dto")
public class CostReportTaskManageDto {

    /**
     * 核算周期
     */
    private String calculateCircle;

    /**
     * 上报任务数量
     */
    private Long totalCount = 0L;

    /**
     * 未下发数量
     */
    private Long unassignedCount = 0L;

    /**
     * 已上报数量
     */
    private Long reportedCount = 0L;

    /**
     * 已下发未上报数量
     */
    private Long toReportCount = 0L;


    /**
     * 待审核数量
     */
    private Long pendingCount = 0L;

    /**
     * 审核通过数量
     */
    private Long approveCount = 0L;

    /**
     * 驳回数量
     */
    private Long rejectCount = 0L;
}
