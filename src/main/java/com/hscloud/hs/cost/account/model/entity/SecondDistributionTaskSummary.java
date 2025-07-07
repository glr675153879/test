package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 二次分配任务概要表
 * </p>
 *
 * @author
 * @since 2023-11-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("second_distribution_task_summary")
@Schema(description = "二次分配任务概要表")
public class SecondDistributionTaskSummary extends Model<SecondDistributionTaskSummary> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "概要id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "关联任务id")
    private Long taskDeptInfoId;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "工号")
    private Long jobNumber;

    @Schema(description = "分配金额")
    private BigDecimal amount;

    @Schema(description = "部门id")
    private Long deptId;

    @Schema(description = "租户id")
    private Long tenantId;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;


}
