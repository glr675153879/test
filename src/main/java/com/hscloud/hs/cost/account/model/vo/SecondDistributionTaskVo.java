package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "二次分配任务结果vo")
public class SecondDistributionTaskVo {

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "工号")
    private Long jobNumber;

    @Schema(description = "二次分配总绩效金额")
    private BigDecimal secondAmount;

    @Schema(description = "管理绩效金额")
    private SecondTaskManagementVo managementVo;

    @Schema(description = "单项绩效金额")
    private SecondTaskSingleVo singleVo;

    @Schema(description = "个人岗位绩效金额")
    private SecondTaskIndividualPostVo individualPostVo;

    @Schema(description = "工作量绩效金额")
    private SecondTaskWorkloadVo workloadVo;

    @Schema(description = "平均绩效金额")
    private SecondTaskAverageVo averageVo;




}
