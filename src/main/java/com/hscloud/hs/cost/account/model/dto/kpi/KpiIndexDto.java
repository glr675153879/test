package com.hscloud.hs.cost.account.model.dto.kpi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
* 指标Model
* @author you
* @since 2024-09-09
*/

@Data
@Schema(description = "指标表")
public class KpiIndexDto{

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "")
    private Long id;

    @TableField(value = "code")
    @Schema(description = "code 用56转 ，带上前缀，核算项 X_ ，分摊指标 F_ ，核算指标 Z_")
    private String code;

    @TableField(value = "index_unit")
    @Schema(description = "指标单位")
    private String indexUnit;

    @TableField(value = "name")
    @Schema(description = "核算指标名称")
    private String name;

    @TableField(value = "account_object")
    @Schema(description = "核算单元对象")
    private String accountObject;

    @TableField(value = "index_property")
    @Schema(description = "指标性质")
    private String indexProperty;

    @TableField(value = "category_code")
    @Schema(description = "指标分组")
    private String categoryCode;

    @TableField(value = "carry_rule")
    @Schema(description = "进位规则 1四舍五入 2向上取整 3向下取整")
    private String carryRule;

    @TableField(value = "reserved_decimal")
    @Schema(description = "指标保留小数")
    private Long reservedDecimal;

    @TableField(value = "index_formula")
    @Schema(description = "指标公式")
    private String indexFormula;

    @TableField(value = "caliber")
    @Schema(description = "口径颗粒度 1人2科室3归集4固定值")
    private String caliber;

    @TableField(value = "type")
    @Schema(description = "指标类型 1非条件指标 2条件指标3分摊指标")
    @NotBlank(message = "type不可空")
    private String type;

    @TableField(value = "imp_flag")
    @Schema(description = "是否为归集指标")
    private String impFlag;

    @TableField(value = "imp_category_code")
    @Schema(description = "归集规则")
    private String impCategoryCode;

    @TableField(value = "second_flag")
    @Schema(description = "是否用于二次分配")
    private String secondFlag;

    @TableField(value = "member_codes")
    @Schema(description = "指标，分摊，指标项合集，json存储")
    private String memberCodes;

    @Schema(description = "说明")
    private String description;

    private Integer seq;
}