package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 初始化完成表
 * </p>
 *
 * @author 
 * @since 2023-09-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "初始化完成")
public class CostBaseInitialize extends Model<CostBaseInitialize> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "应用编码")
    private String appCode;

    @Schema(description = "业务编码")
    private String bizCode;

    @Schema(description = "初始化是否完成：0：未完成  1：完成")
    private Boolean status;


}
