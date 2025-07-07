package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 核算任务关联表
 * </p>
 *
 * @author author
 * @since 2023-11-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("cost_account_task_config")
@Schema(description ="核算任务关联表")
public class CostAccountTaskConfig extends Model<CostAccountTaskConfig> {

    private static final long serialVersionUID = 1L;

    @Schema(description  = "主键id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description  = "核算任务id")
    private Long taskId;

    @Schema(description = "核算对象类型")
    private String accountObjectType;

    @Schema(description  = "任务分组id")
    private Long taskGroupId;

    @Schema(description  = "核算方案id")
    private Long planId;

    @Schema(description = "核算对象id集合")
    private String accountObjectIds;


}
