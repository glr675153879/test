package com.hscloud.hs.cost.account.model.entity.kpi;

import com.baomidou.mybatisplus.annotation.*;
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
* 归集规则Model
* @author you
* @since 2024-09-13
*/

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "归集规则表")
@TableName("kpi_imputation_rule")
public class KpiImputationRule extends Model<KpiImputationRule>{

    @TableId(value = "id", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;

    @TableField(value = "category_code")
    @Column(comment="归集分组code", type = MySqlTypeConstant.VARCHAR, length = 255 , isNull = false )
    private String categoryCode;

    @TableField(value = "rule_name")
    @Column(comment="规则名称", type = MySqlTypeConstant.VARCHAR, length = 50)
    private String ruleName;

    @TableField(value = "seq")
    @Column(comment="优先级", type = MySqlTypeConstant.INT , isNull = false )
    private Long seq;

    @TableField(value = "rule_type")
    @Column(comment="规则类型 1特殊归集2无需归集 3归集科室 群组专用", type = MySqlTypeConstant.CHAR, length = 255  , isNull = false )
    private String ruleType;

    @TableField(value = "reason", updateStrategy = FieldStrategy.ALWAYS)
    @Column(comment="归集原因", type = MySqlTypeConstant.VARCHAR, length = 500)
    private String reason;

    @TableField(value = "people", updateStrategy = FieldStrategy.ALWAYS)
    @Column(comment = "人员 1个人 2人员分组", type = MySqlTypeConstant.CHAR, length = 1, isNull = false)
    private String people;

    @TableField(value = "dept_id", updateStrategy = FieldStrategy.ALWAYS)
    @Column(comment = "科室单元id", type = MySqlTypeConstant.BIGINT)
    private Long deptId;

    @TableField(value = "member_ids", updateStrategy = FieldStrategy.ALWAYS)
    @Column(comment="逗号切割存一份 member存一份", type = MySqlTypeConstant.VARCHAR, length = 2000)
    private String memberIds;

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

    @TableField(value = "del_falg")
    @Column(comment="删除", type = MySqlTypeConstant.CHAR, length = 1 )
    private String delFalg;

    @TableField(value = "busi_type")
    @Column(comment = "业务类型，1，一次绩效，2，科室成本", isNull = false, defaultValue = "1", type = MySqlTypeConstant.VARCHAR, length = 20)
    @Index
    private String busiType= "1";

}