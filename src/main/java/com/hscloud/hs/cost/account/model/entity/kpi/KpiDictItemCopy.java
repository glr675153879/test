/*
 *    Copyright (c) 2018-2025, lengleng All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the hscloud.com developer nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Author: lengleng (wangiegie@gmail.com)
 */
package com.hscloud.hs.cost.account.model.entity.kpi;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 字典项
 *
 * @author lengleng
 * @date 2019/03/19
 */
@Data
@TenantTable
@Schema(description = "字典项")
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class KpiDictItemCopy extends Model<KpiDictItemCopy> {

    private static final long serialVersionUID = 1L;

    /**
     * 编号
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "字典项id")
    private Long id;

    /**
     * 所属字典类id
     */
    @Schema(description = "所属字典类id")
    @TableField(value = "dict_id")
    private Long dictId;


    /**
     * 数据值
     */
    @Schema(description = "数据值")
    @JsonProperty(value = "value")
    @TableField(value = "item_value")
    private String itemValue;

    @Schema(description = "字典code")
    @JsonProperty(value = "code")
    @TableField(value = "item_code")
    private String itemCode;

    @Schema(description = "字典上级code")
    @JsonProperty(value = "parentCode")
    @TableField(value = "parent_code")
    private String parentCode;

    /**
     * 标签名
     */
    @Schema(description = "标签名")
    private String label;

    /**
     * 类型
     */
    @Schema(description = "类型")
    @TableField(value = "dict_type")
    private String dictType;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

    /**
     * 排序（升序）
     */
    @Schema(description = "排序值，默认升序")
    @TableField(value = "sort_order")
    private Integer sortOrder;

    /**
     * 创建人
     */
    @TableField(value = "create_by",fill = FieldFill.INSERT)
    @Schema(description = "创建人")
    private String createBy;

    /**
     * 修改人
     */
    @TableField(value = "update_by",fill = FieldFill.UPDATE)
    @Schema(description = "修改人")
    private String updateBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time",fill = FieldFill.UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 备注信息
     */
    @Schema(description = "备注信息")
    private String remarks;

    /**
     * 删除标记
     */
    @TableLogic
    @TableField(value = "del_flag",fill = FieldFill.INSERT)
    @Schema(description = "删除标记,1:已删除,0:正常")
    private String delFlag;


    /**
     * 启停用标识（0-启用,1-停用）
     */
    @Schema(description = "启停用标识,1:停用,0:启用")
    private String status;

    @Schema(description = "人员系数")
    @TableField(value = "personnel_factor_value")
    @Column(comment = "人员系数", type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal personnelFactorValue;

    @Schema(description = "绩效补贴")
    @TableField(value = "performance_subsidy_value")
    @Column(comment = "绩效补贴", type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal performanceSubsidyValue;

    private Date copyDate;

    private Long taskChildId;

    @Schema(description = "租户ID")
    private Long tenantId;

}
