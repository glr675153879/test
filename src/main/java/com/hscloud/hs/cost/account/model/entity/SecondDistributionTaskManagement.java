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
 * 二次分配任务管理绩效表
 * </p>
 *
 * @author
 * @since 2023-11-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("second_distribution_task_management")
@Schema(description = "二次分配任务管理绩效表")
public class SecondDistributionTaskManagement extends Model<SecondDistributionTaskManagement> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "管理绩效id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "关联任务id")
    private Long taskUnitRelateId;

    @Schema(description = "核算指标id")
    private Long indexId;

    @Schema(description = "核算指标名称")
    private String indexName;

    @Schema(description = "核算方案id")
    private String planId;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "工号")
    private Long jobNumber;

    @Schema(description = "管理岗位id")
    private Long positionId;

    @Schema(description = "管理岗位名称")
    private String positionName;

    @Schema(description = "管理绩效金额")
    private BigDecimal amount;

    @Schema(description = "计算单位")
    private String unit;

    @Schema(description = "核算单元id")
    private Long unitId;

    @Schema(description = "类型：一次分配、二次分配")
    private String type;

    @Schema(description = "租户id")
    private Long tenantId;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;


}
