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
package com.hscloud.hs.cost.account.model.dto.kpi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

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
public class KpiDictItemList extends Model<KpiDictItemList> {

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
    private Long dictId;


    /**
     * 数据值
     */
    @Schema(description = "数据值")
    @JsonProperty(value = "value")
    private String itemValue;

    @Schema(description = "字典code")
    @JsonProperty(value = "code")
    private String itemCode;

    @Schema(description = "字典上级code")
    @JsonProperty(value = "parentCode")
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
    private Integer sortOrder;

    /**
     * 备注信息
     */
    @Schema(description = "备注信息")
    private String remarks;


    @Schema(description = "人员系数")
    private BigDecimal  personnelFactoryValue;

    @Schema(description = "绩效补贴")
    private BigDecimal performanceSubsidyValue;

    private List<KpiDictItemList> children;
}
