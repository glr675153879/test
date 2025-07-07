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
 * 核算任务关联指标表
 * </p>
 *
 * @author author
 * @since 2023-11-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("cost_account_task_config_index")
@Schema(description="核算任务关联指标表")
public class CostAccountTaskConfigIndex extends Model<CostAccountTaskConfigIndex> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "核算任务配置id")
    private Long taskConfigId;

    @Schema(description = "指标id")
    private Long indexId;

    @Schema(description = "核算类型")
    private String accountObjectType;

    @Schema(description = "是否关联 0:否  1:是")
    private String isRelevance;

    @Schema(description = "关联任务id")
    private Long relevanceTaskId;


}
