package com.hscloud.hs.cost.account.model.vo.kpi;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAllocationRule;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

/**
* 分摊公式Model
* @author you
* @since 2024-09-09
*/

@Data
@Schema(description = "分摊公式表")
public class KpiAllocationRuleListVO{

    @Schema(description = "")
    private Long id;

    @Schema(description = "分摊项/分摊指标")
    private String allocationItem;

    @Schema(description = "分摊类型 1全院分摊 2医护分摊3病区分摊借床分摊4门诊共用分摊")
    private String type;

    @Schema(description = "分摊规则")
    private String rule;

    @Schema(description = "摊出核算单元")
    private String outMember;

    @Schema(description = "摊入核算单元")
    private String imMember;

    @Schema(description = "应用方案")
    private String plan;

/*************中转***************/

    @TableField(value = "allocation_indexs")
    @Column(comment="分摊指标", type = MySqlTypeConstant.TEXT )
    private String allocationIndexs;

    @TableField(value = "allocation_items")
    @Column(comment="分摊项", type = MySqlTypeConstant.TEXT )
    private String allocationItems;

    @TableField(value = "out_members_emp")
    @Column(comment="摊出人员(关联表中out_members_emp)", type = MySqlTypeConstant.TEXT )
    private String outMembersEmp;

    @TableField(value = "out_members_dept")
    @Column(comment="摊出科室(关联表中out_members_dept)", type = MySqlTypeConstant.TEXT )
    private String outMembersDept;

    @TableField(value = "in_members_emp")
    @Column(comment="摊入人员(关联表中in_members_emp)", type = MySqlTypeConstant.TEXT )
    private String inMembersEmp;

    @TableField(value = "in_members_dept")
    @Column(comment="摊入科室(关联表中in_members_dept)", type = MySqlTypeConstant.TEXT )
    private String inMembersDept;

    @TableField(value = "out_members_imp")
    @Column(comment="摊出归集(关联表中out_members_imp)", type = MySqlTypeConstant.TEXT )
    private String outMembersImp;

    private String docCode;

    public static KpiAllocationRuleListVO convertByKpiAllocationRule(KpiAllocationRule t){
        KpiAllocationRuleListVO kpiAllocationRuleListVO = BeanUtil.copyProperties(t, KpiAllocationRuleListVO.class);
        return kpiAllocationRuleListVO;
    }

    @Data
    public static class Yhgx{
        private String docCode;
        private Long docAccountId;
        private String docAccountName;
        private Long nurseAccountId;
        private String nurseAccountName;
    }
}