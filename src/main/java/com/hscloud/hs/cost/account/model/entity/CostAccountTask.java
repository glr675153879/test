package com.hscloud.hs.cost.account.model.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TenantTable
@Schema(description = "核算任务")
public class CostAccountTask extends Model<CostAccountTask> {

    @Schema(description = "核算任务id")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "核算任务名称")
    private String accountTaskName;

    @Schema(description = "核算类型")
    private String accountType;

    @Schema(description = "核算开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime accountStartTime;

    @Schema(description = "核算结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime accountEndTime;

    @Schema(description = "核算单元id集合")
    private String unitIds;

    @Schema(description = "归集单元id集合")
    private String collectionIds;

    @Schema(description = "核算方案id")
    private Long planId;


    @Schema(description = "核算维度 MONTH：月度 QUARTER：季度 YEAR：DAY：日度")
    private String dimension;

    @Schema(description = "具体维度")
    private String detailDim;

    @Schema(description = "是否支持统计 0：不支持 1：支持")
    private String supportStatistics;

    @Schema(description = "异常原因")
    private String reason;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人")
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 状态
     */
    @Schema(description = "状态 CALCULATING：计算中 EXCEPTION：异常 COMPLETED：已完成 TO_BE_SUBMITTED：待提交")
    private String status;

    /**
     * 0-正常，1-删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "删除标记,1:已删除,0:正常")
    private String delFlag;


    /**
     * 租户ID
     */
    @Schema(description = "租户ID")
    private Long tenantId;
}
