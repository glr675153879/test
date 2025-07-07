package com.hscloud.hs.cost.account.model.entity.kpi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.Index;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsAutoIncrement;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.math.BigDecimal;

import java.util.Date;
import java.util.List;
/**
* 分摊公式备份Model
* @author you
* @since 2024-09-13
*/

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "分摊公式表备份")
@TableName("kpi_allocation_rule_copy")
public class KpiAllocationRuleCopy extends Model<KpiAllocationRuleCopy>{

    @TableId(value = "zj", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long zj;

    @TableField(value = "id")
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;

    @TableField(value = "index_code")
    @Column(comment="", type = MySqlTypeConstant.VARCHAR, length = 255 )
    private String indexCode;

    @TableField(value = "formula")
    @Column(comment="公式", type = MySqlTypeConstant.TEXT )
    private String formula;

    @TableField(value = "check_flag")
    @Column(comment="是否校验", type = MySqlTypeConstant.CHAR, length = 1 )
    private String checkFlag;

    @TableField(value = "type")
    @Column(comment="分摊类型 1全院分摊 2医护分摊 3病区分摊或借床分摊 4门诊共用分摊", type = MySqlTypeConstant.CHAR, length = 1 )
    private String type;

    @TableField(value = "rule")
    @Column(comment="分摊规则 1收入，2平均，3自定义比例", type = MySqlTypeConstant.CHAR, length = 1)
    private String rule;

    @TableField(value = "ratio")
    @Column(comment="分摊比例",type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal ratio;

    @TableField(value = "allocation_indexs")
    @Column(comment="分摊指标", type = MySqlTypeConstant.TEXT )
    private String allocationIndexs;

    @TableField(value = "allocation_items")
    @Column(comment="分摊项", type = MySqlTypeConstant.TEXT )
    private String allocationItems;

    @TableField(value = "plan_code")
    @Column(comment="方案code", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String planCode;

    @TableField(value = "in_members_emp")
    @Column(comment="摊入人员", type = MySqlTypeConstant.TEXT )
    private String inMembersEmp;

    @TableField(value = "in_members_dept")
    @Column(comment="摊入科室", type = MySqlTypeConstant.TEXT )
    private String inMembersDept;

    @TableField(value = "out_members_imp")
    @Column(comment="摊出归集", type = MySqlTypeConstant.TEXT )
    private String outMembersImp;

    @TableField(value = "out_members_emp")
    @Column(comment="摊出人员", type = MySqlTypeConstant.TEXT )
    private String outMembersEmp;

    @TableField(value = "out_members_dept")
    @Column(comment="摊出科室", type = MySqlTypeConstant.TEXT )
    private String outMembersDept;

    @TableField(value = "doc_code")
    @Column(comment="医护关系code", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String docCode;

    @TableField(value = "member_codes")
    @Column(comment="指标，指标项合集，json存储", type = MySqlTypeConstant.TEXT )
    private String memberCodes;

    @TableField(value = "created_id")
    @Column(comment="创建人", type = MySqlTypeConstant.BIGINT)
    private Long createdId;

    @TableField(value = "created_date")
    @Column(comment="创建时间", type = MySqlTypeConstant.DATETIME)
    private Date createdDate;

    @TableField(value = "updated_id")
    @Column(comment="更新人", type = MySqlTypeConstant.BIGINT)
    private Long updatedId;

    @TableField(value = "updated_date")
    @Column(comment="更新时间", type = MySqlTypeConstant.DATETIME)
    private Date updatedDate;

    @TableField(value = "tenant_id")
    @Column(comment="租户号", type = MySqlTypeConstant.BIGINT)
    private Long tenantId;

    @TableField(value = "task_child_id")
    @Column(comment="子任务id", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long taskChildId;

    @TableField(value = "copy_date")
    @Column(comment="备份时间", type = MySqlTypeConstant.DATETIME , isNull = false )
    private Date copyDate;
}