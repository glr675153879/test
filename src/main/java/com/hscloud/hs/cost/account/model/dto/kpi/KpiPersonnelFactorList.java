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

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 字典项
 *
 * @author lengleng
 * @date 2019/03/19
 */
@Data
@TenantTable
@Schema(description = "人员参数列表")
@EqualsAndHashCode(callSuper = true)
public class KpiPersonnelFactorList extends Model<KpiPersonnelFactorList> {

    private static final long serialVersionUID = 1L;


    @Schema(description = "id")
    private Long id;


    @Schema(description = "字典code")
    @JsonProperty(value = "code")
    private String itemCode;


    @Schema(description = "字典项名称")
    @JsonProperty(value = "label")
    private String label;



    @Schema(description = "字典上级code")
    @JsonProperty(value = "parentCode")
    private String parentCode;

    /**
     * 标签名
     */
    @Schema(description = "人员姓名")
    private String name;


    @Schema(description = "userId")
    private Long userId;

    /**
     * 类型
     */
    @Schema(description = "类型 1系统规则 2 自定义规则")
    private String type;

    /**
     * 描述
     */
    @Schema(description = "值")
    private BigDecimal value;

    /**
     * 类型
     */
    @Schema(description = "新加补贴类型 1系统规则 2 自定义规则")
    private String subsidyType;

    /**
     * 描述
     */
    @Schema(description = "新加补贴值")
    private BigDecimal subsidyValue;

    @Schema(description = "字典人员系数")
    private BigDecimal personnelFactoryValue;

    @Schema(description = "字典补贴系数")
    private BigDecimal performanceSubsidyValue;

    private String deptName;

}
