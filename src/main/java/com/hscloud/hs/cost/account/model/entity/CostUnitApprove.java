package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * @author YJM
 * @date 2023-09-05 17:07
 */
public class CostUnitApprove {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "科室单元ID")
    private Long accountUnitId;

    @Schema(description = "申请人id")
    private Long createId;

    @Schema(description = "申请类型:CREATE,UPDATE,DELETE,ENABLE,DISABLE")
    private String type;

    @Schema(description = "变更明细")
    private String operateDetail;

    @Schema(description = "变更处理结果,忽略:0  已变更:1 待变更:2")
    private Short status;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
