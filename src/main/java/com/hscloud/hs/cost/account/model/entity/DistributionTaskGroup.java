package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 任务分组
 * </p>
 *
 * @author author
 * @since 2023-11-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("distribution_task_group")
@Schema(description = "任务分组")
public class DistributionTaskGroup extends Model<DistributionTaskGroup> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "任务分组名称")
    private String name;

    @Schema(description = "任务类型")
    private String type;

    @Schema(description = "核算对象")
    private String accountObject;

    @Schema(description = "任务结果指标项")
    private Long indexId;

    @Schema(description = "任务结果指标名称")
    private String indexName;

    @Schema(description = "状态  0 启用  1 停用")
    private String status;

    @Schema(description = "创建人id")
    private Long createUserId;

    @Schema(description = "创建人名称")
    private String createUserName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;


}
