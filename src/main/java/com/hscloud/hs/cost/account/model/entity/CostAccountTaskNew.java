package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 核算任务表(新)
 * </p>
 *
 * @author author
 * @since 2023-11-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("cost_account_task_new")
@Schema(description="核算任务表(新)")
public class CostAccountTaskNew extends Model<CostAccountTaskNew> {

    private static final long serialVersionUID = 1L;

    @Schema(description  = "核算任务id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description  = "核算任务名称")
    private String accountTaskName;

    @Schema(description  = "核算开始时间")
    private LocalDateTime accountStartTime;

    @Schema(description  = "核算结束时间")
    private LocalDateTime accountEndTime;

    @Schema(description  = "异常原因")
    private String reason;

    @Schema(description  = "创建人")
    private String createBy;

    @Schema(description  = "创建时间")
    private LocalDateTime createTime;

    @Schema(description  = "二次分配下发时间")
    private LocalDateTime secondTime;

    @Schema(description  = "是否下发二次分配 1是 0否")
    private String ifSecond;

    @Schema(description  = "状态")
    private String status;

    @Schema(description  = "是否删除：0：未删除 1：删除")
    private String delFlag;

    @Schema(description = "编辑到达层数")
    private String step;

    @Schema(description  = "租户id")
    private Long tenantId;

    @Column(comment = "发放单元id")
    @Schema(description = "发放单元id")
    private String grantUnitIds;

    @Column(comment = "发放单元名称")
    @Schema(description = "发放单元名称")
    private String grantUnitNames;
}
