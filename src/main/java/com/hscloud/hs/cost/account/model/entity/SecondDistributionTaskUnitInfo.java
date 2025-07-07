package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 二次分配任务和科室单元关联表
 * </p>
 *
 * @author
 * @since 2023-11-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("second_distribution_task_unit_info")
@Schema(description = "二次分配任务和科室单元关联表")
public class SecondDistributionTaskUnitInfo extends Model<SecondDistributionTaskUnitInfo> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "任务id")
    private Long taskId;

    @Schema(description = "方案id")
    private Long planId;

    @Schema(description = "科室单元id")
    private Long unitId;

    @Schema(description = "提交人员id")
    private Long userId;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "工号")
    private String jobNumber;

    @Schema(description = "科室名称")
    private String unitName;

    @Schema(description = "流程实例id")
    private Long processInstanceId;

    @Schema(description = "流程模板code")
    private String processCode;

    @Schema(description = "状态：未提交、待分配、待审批、审批驳回、审批通过")
    private String status;

    @Schema(description = "总共可分配金额")
    private BigDecimal totalAmount;

    @Schema(description = "租户id")
    private Long tenantId;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;


}
