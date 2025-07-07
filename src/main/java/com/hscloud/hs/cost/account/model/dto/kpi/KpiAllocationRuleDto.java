package com.hscloud.hs.cost.account.model.dto.kpi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
* 分摊公式Model
* @author you
* @since 2024-09-09
*/

@Data
@Schema(description = "分摊公式表")
public class KpiAllocationRuleDto extends Model<KpiAllocationRuleDto>{

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "")
    private Long id;

    @TableField(value = "formula")
    @Schema(description = "公式")
    private String formula;

    @Schema(description = "指标code")
    private String indexCode;

    @TableField(value = "check_flag")
    @Schema(description = "是否校验")
    private String checkFlag;

    @TableField(value = "type")
    @Schema(description = "分摊类型 1全院分摊 2医护分摊3病区分摊4借床分摊5门诊共用分摊")
    private String type;

    @TableField(value = "rule")
    @Schema(description = "分摊规则")
    private String rule;

    @TableField(value = "ratio")
    @Schema(description = "分摊比例")
    private BigDecimal ratio;

    @TableField(value = "allocation_indexs")
    @Schema(description = "分摊指标")
    private String allocationIndexs;

    @TableField(value = "allocation_items")
    @Schema(description = "分摊项")
    private String allocationItems;

    @TableField(value = "plan_code")
    @Schema(description = "方案code")
    private String planCode;

    @TableField(value = "in_members_emp")
    @Schema(description = "摊入人员")
    private String inMembersEmp;

    @TableField(value = "in_members_dept")
    @Schema(description = "摊入科室")
    private String inMembersDept;

    @TableField(value = "out_members_imp")
    @Schema(description = "摊出归集")
    private String outMembersImp;

    @TableField(value = "out_members_emp")
    @Schema(description = "摊出人员")
    private String outMembersEmp;

    @TableField(value = "out_members_dept")
    @Schema(description = "摊出科室")
    private String outMembersDept;

    @TableField(value = "doc_code")
    @Schema(description = "医护关系code")
    private String docCode;

    @TableField(value = "member_codes")
    @Schema(description = "指标，指标项合集，json存储")
    private String memberCodes;

    @TableField(value = "allocation_object_caliber")
    @Column(comment = "分摊项颗粒度", type = MySqlTypeConstant.VARCHAR, length = 1 )
    private String allocationObjectCaliber;

    @Schema(description = "公式项属性")
    private List<KpiFormulaItemDto> kpiFormulaItemDtos;
}