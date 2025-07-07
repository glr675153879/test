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

import java.math.BigDecimal;
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
public class KpiUserFactor extends Model<KpiUserFactor> {

    private static final long serialVersionUID = 1L;

    /**
     * 编号
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private Long id;

    /**
     * 科室code
     */
    @Schema(description = "科室code")
    private Long deptId;

    @Schema(description = "user,office,coefficient,subsidy 人员、职务、系数、补贴")
    private String type;

    @TableField(value = "user_id")
    @Column(comment = "用户id", type = MySqlTypeConstant.BIGINT)
    private Long userId;


    /**
     * 姓名
     */


    @Schema(description = "对应一级字典type")
    @TableField(value = "dict_type")
    private String dictType;

    @Schema(description = "对应二级字典code")
    private String itemCode;

    @Schema(description = "数值")
    @Column(comment = "数值", type = MySqlTypeConstant.DECIMAL, length = 20, decimalLength = 6)
    private BigDecimal value;

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
