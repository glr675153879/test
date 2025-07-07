package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.ehcache.shadow.org.terracotta.offheapstore.paging.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "绩效总值报表查询dto")
public class DistributionStatementQueryDto  {

    @Schema(description = "任务id")
    private Long taskId;

    @Schema(description = "开始日期")
    private LocalDateTime startTime;

    @Schema(description = "结束日期")
    private LocalDateTime endTime;

    @Schema(description = "同比 RISE：上升 FALL：下降 不限：空")
    private String yearOverYear;

    @Schema(description = "环比 RISE：上升 FALL：下降 不限：空")
    private String monthOverMonth;

    @Schema(description = "开始总核算值")
    private BigDecimal startTotalCount;

    @Schema(description = "结束总核算值")
    private BigDecimal endTotalCount;


    /**
     * 科室绩效总值
     */
    @Schema(description = "科室单元id")
    private Long unitId;

    @Schema(description = "科室单元名称")
    private String unitName;


    /**
     * 员工绩效总值
     */
    @Schema(description = "时间周期")
    private LocalDateTime time;

}
