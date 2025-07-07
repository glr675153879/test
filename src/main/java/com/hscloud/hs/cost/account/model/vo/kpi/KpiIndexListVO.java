package com.hscloud.hs.cost.account.model.vo.kpi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@Accessors(chain = true)
@Schema(description = "指标表")
public class KpiIndexListVO{

    @TableId(value = "id", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;

    @TableField(value = "index_unit")
    @Column(comment="指标单位", type = MySqlTypeConstant.VARCHAR, length = 255)
    @Schema(description = "指标单位")
    private String indexUnit;

    @Schema(description = "指标分组")
    private String category;

    @Schema(description = "指标分组名称")
    private String categoryName;

    @TableField(value = "name")
    @Column(comment="核算指标名称", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String name;

    private String code;

    @Schema(description = "进位规则 1四舍五入 2向上取整 3向下取整")
    private String carryRule;

    @Schema(description = "指标保留小数")
    private Long reservedDecimal;

    @TableField(value = "account_object")
    @Column(comment="核算单元对象", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String accountObject;

    @TableField(value = "index_property")
    @Column(comment="指标性质", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String indexProperty;

    @TableField(value = "caliber")
    @Schema(description = "口径颗粒度 1人2科室3归集4固定值")
    private String caliber;

    @TableField(value = "category_code")
    @Column(comment="指标分组", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String categoryCode;

    @Schema(description = "状态：0：启用  1:停用")
    private String status;

    @Schema(description = "指标类型 1非条件指标 2条件指标3分摊指标")
    private String type;

    private String impFlag;

    private String secondFlag;

    private Date updatedDate;

    private Integer seq;
}