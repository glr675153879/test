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
 * 二次分配任务表
 * </p>
 *
 * @author
 * @since 2023-11-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("second_distribution_task")
@Schema(description = "二次分配任务表")
public class SecondDistributionTask extends Model<SecondDistributionTask> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "任务id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "任务名称")
    private String name;

    @Schema(description = "任务类型")
    private String type;

    @Schema(description = "任务周期")
    private String taskPeriod;

    @Schema(description = "总共可分配值")
    private BigDecimal totalAmount;

    @Schema(description = "状态:UNDERWAY：进行中；FINISHED：已完成：")
    private String status;

    @Schema(description = "科室单元集合")
    private String unitIds;

    @Schema(description = "租户id")
    private Long tenantId;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

}
