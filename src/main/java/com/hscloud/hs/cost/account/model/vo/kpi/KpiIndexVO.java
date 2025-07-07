package com.hscloud.hs.cost.account.model.vo.kpi;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsAutoIncrement;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
* 指标Model
* @author you
* @since 2024-09-13
*/

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "指标表")
@TableName("kpi_index")
public class KpiIndexVO extends Model<KpiIndexVO>{

    @TableId(value = "id", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;

    @TableField(value = "code")
    @Column(comment="code 用56转 ，带上前缀，核算项 X_ ，分摊指标 F_ ，核算指标 Z_", type = MySqlTypeConstant.VARCHAR, length = 50 , isNull = false )
    private String code;

    @TableField(value = "index_unit")
    @Column(comment="指标单位", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String indexUnit;

    @TableField(value = "name")
    @Column(comment="核算指标名称", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String name;

    @TableField(value = "account_object")
    @Column(comment="核算单元对象", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String accountObject;

    @TableField(value = "index_property")
    @Column(comment="指标性质", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String indexProperty;

    @TableField(value = "category_code")
    @Column(comment="指标分组", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String categoryCode;

    @TableField(value = "carry_rule")
    @Column(comment="进位规则 1四舍五入 2向上取整 3向下取整", type = MySqlTypeConstant.CHAR, length = 1 )
    private String carryRule;

    @TableField(value = "reserved_decimal")
    @Column(comment="指标保留小数", type = MySqlTypeConstant.INT)
    private Integer reservedDecimal;

    @TableField(value = "index_formula")
    @Column(comment="指标公式", type = MySqlTypeConstant.VARCHAR, length = 4000)
    private String indexFormula;

    @TableField(value = "status")
    @Column(comment="状态：0：启用  1:停用", type = MySqlTypeConstant.CHAR, length = 1 )
    private String status;

    @TableField(value = "del_flag")
    @Column(comment="是否删除：0：未删除 1：删除", type = MySqlTypeConstant.CHAR, length = 1 )
    private String delFlag;

    @TableField(value = "created_id",fill = FieldFill.INSERT)
    @Column(comment="创建人", type = MySqlTypeConstant.BIGINT)
    private Long createdId;

    @TableField(value = "created_date",fill = FieldFill.INSERT)
    @Column(comment="创建时间", type = MySqlTypeConstant.DATETIME)
    private Date createdDate;

    @TableField(value = "updated_id",fill = FieldFill.INSERT_UPDATE)
    @Column(comment="更新人", type = MySqlTypeConstant.BIGINT)
    private Long updatedId;

    @TableField(value = "updated_date",fill = FieldFill.INSERT_UPDATE)
    @Column(comment="更新时间", type = MySqlTypeConstant.DATETIME)
    private Date updatedDate;

    @TableField(value = "tenant_id")
    @Column(comment="租户号", type = MySqlTypeConstant.BIGINT)
    private Long tenantId;

    @TableField(value = "caliber")
    @Column(comment="口径颗粒度 1人2科室3归集4固定值", type = MySqlTypeConstant.CHAR, length = 1 )
    private String caliber;

    @TableField(value = "type")
    @Column(comment="指标类型 1非条件指标 2条件指标3分摊指标", type = MySqlTypeConstant.CHAR, length = 1 )
    private String type;

    @TableField(value = "imp_flag")
    @Column(comment="是否为归集指标", type = MySqlTypeConstant.CHAR, length = 1 )
    private String impFlag;

    @TableField(value = "imp_category_code")
    @Column(comment="归集规则", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String impCategoryCode;

    @TableField(value = "second_flag")
    @Column(comment="是否用于二次分配", type = MySqlTypeConstant.CHAR, length = 1 )
    private String secondFlag;

    @TableField(value = "member_codes")
    @Column(comment="指标，分摊，指标项合集，json存储", type = MySqlTypeConstant.TEXT )
    private String memberCodes;

    @TableField(value = "description")
    @Column(comment="指标说明", type = MySqlTypeConstant.VARCHAR, length = 200)
    private String description;

    private Integer seq;

    @Schema(description = "0允许修改 1不允许")
    private Integer allowModify = 0;
}