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
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 字典表
 *
 * @author lengleng
 * @date 2019/03/19
 */
@Data
@TenantTable
@Schema(description = "字典类型")
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("kpi_dict")
public class KpiDict extends Model<KpiDict> {

    private static final long serialVersionUID = 1L;

    /**
     * 编号
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "字典编号")
    private Long id;

    /**
     * 类型
     */
    @Schema(description = "字典类型")
    @TableField(value = "dict_type")
    private String dictType;

    /**
     * 描述
     */
    @Schema(description = "字典描述")
    private String description;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @TableField(value = "update_time",fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;


    /**
     * 备注信息
     */
    @Schema(description = "备注信息")
    private String remarks;

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
     * 删除标记
     */
    @TableLogic
    @TableField(value = "del_flag",fill = FieldFill.INSERT)
    @Schema(description = "删除标记,1:已删除,0:正常")
    private String delFlag;

    @Schema(description = "启停用标识,1:停用,0:启用")
    private String status;


    /**
     * 租户ID
     */
    @Schema(description = "租户ID")
    @TableField(value = "tenant_id")
    private Long tenantId;


    @Schema(description = "是否配置绩效签发 1配置")
    @TableField(value = "kpi_sign")
    @Column(comment = "是否配置绩效签发 1配置", type = MySqlTypeConstant.CHAR, length = 10)
    private String kpiSign;

    @Schema(description = "是否配置人员系数 1配置")
    @TableField(value = "personnel_factor")
    private String personnelFactor;

    @Schema(description = "是否配置绩效补贴  1配置")
    @TableField(value = "performance_subsidy")
    private String performanceSubsidy;


}
