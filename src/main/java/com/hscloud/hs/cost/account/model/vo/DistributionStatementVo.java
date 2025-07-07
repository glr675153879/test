package com.hscloud.hs.cost.account.model.vo;

import com.hscloud.hs.cost.account.model.pojo.MonthRatio;
import com.hscloud.hs.cost.account.model.pojo.YearRatio;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "绩效总值报表vo")
public class DistributionStatementVo {

    @Schema(description = "核算任务id")
    private Long id;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "统计维度")
    private String dimension;

    @Schema(description = "总核算值")
    private BigDecimal totalCount = BigDecimal.ZERO;

    @Schema(description = "同比")
    private YearRatio yearRatio;

    @Schema(description = "环比")
    private MonthRatio monthRatio;


    /**
     * 医院绩效总值
     */
    @Schema(description = "目标值 自行设定")
    private BigDecimal targetCount;

    @Schema(description = "警告标志")
    private String warnFlag;


    /**
     * 科室绩效总值
     */
    @Schema(description = "核算单元id")
    private Long unitId;

    @Schema(description = "核算单元名称")
    private String unitName;


    /**
     * 员工绩效总值
     */
    @Schema(description = "核算分组id")
    private Long groupId;

    @Schema(description = "核算分组名称")
    private String groupName;

    @Schema(description = "员工id")
    private Long userId;

    @Schema(description = "员工名称")
    private String userName;

}
