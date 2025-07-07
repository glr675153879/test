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
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 字典表
 *
 * @author lengleng
 * @date 2019/03/19
 */
@Data
@TenantTable
@Schema(description = "人员系数")
@EqualsAndHashCode(callSuper = true)
public class KpiUserFactorOld2 extends Model<KpiUserFactorOld2> {

    private static final long serialVersionUID = 1L;

    /**
     * 编号
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "字典编号")
    private Long id;

    /**
     * 科室code
     */
    @Schema(description = "科室code")
    private String unitCode;

    /**
     * 姓名
     */
    @Schema(description = "姓名")
    private String name;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    /**
     * 职务
     */
    @Schema(description = "职务")
    private String office;


    @Schema(description = "职务系数")
    private String officeValue;

    /**
     * 职称
     */
    @Schema(description = "职称")
    private String jobTitle;


    @Schema(description = "职称系数")
    private String jobTitleValue;

    @Schema(description = "事业单位岗位")
    private String post;

    @Schema(description = "事业单位岗位系数")
    private String postValue;


    @Schema(description = "绩效岗位")
    private String performance;

    @Schema(description = "绩效岗位系数")
    private String performanceValue;

    @Schema(description = "用工性质")
    private String employmentNature;

    @Schema(description = "用工系数")
    private String employmentValue;

    @Schema(description = "人员类别")
    private String personType;

    @Schema(description = "人员系数")
    private String personValue;

    @Schema(description = "科室综合系数")
    private String unitValue;


    /**
     * 删除标记
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "删除标记,1:已删除,0:正常")
    private String delFlag;



    /**
     * 租户ID
     */
    @Schema(description = "租户ID")
    private Long tenantId;




}
