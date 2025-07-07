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
/**
* 核算单元(cost_account_unit)Model
* @author you
* @since 2024-09-13
*/

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "核算单元(cost_account_unit)")
@TableName("kpi_account_unit")
public class KpiAccountUnit extends Model<KpiAccountUnit>{

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;

    @TableField(value = "name")
    @Column(comment="科室单元名称", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String name;

    @TableField(value = "category_code")
    @Column(comment="核算分组代码", type = MySqlTypeConstant.VARCHAR, length = 255 , isNull = false )
    private String categoryCode;

    @TableField(value = "third_code",updateStrategy = FieldStrategy.ALWAYS)
    @Column(comment="三方编码", type = MySqlTypeConstant.VARCHAR, length = 255 , isNull = true )
    private String thirdCode;


    @TableField(value = "account_type_code",updateStrategy = FieldStrategy.ALWAYS)
    @Column(comment="科室单元类型", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String accountTypeCode;

    @TableField(value = "responsible_person_id")
    @Column(comment="负责人", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String responsiblePersonId;

    @TableField(value = "responsible_person_type")
    @Column(comment="负责人类型dept | user | role", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String responsiblePersonType;

    @TableField(value = "status")
    @Column(comment="启停用标记，0启用，1停用", type = MySqlTypeConstant.CHAR, length = 1,defaultValue = "0")
    private String status;

    @TableField(value = "del_flag",fill = FieldFill.INSERT)
    @TableLogic(value = "0", delval = "1")
    @Column(comment="删除标记，0未删除，1已删除", type = MySqlTypeConstant.CHAR, length = 1,defaultValue = "0")
    private String delFlag;

    @TableField(value = "initialized")
    @Column(comment="初始化状态: Y:已初始化  N:未初始化", type = MySqlTypeConstant.CHAR, length = 1 )
    private String initialized;

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

    @TableField(value = "responsible_person_name")
    @Column(comment="负责人类型dept | user | role", type = MySqlTypeConstant.VARCHAR, length = 500)
    private String responsiblePersonName;

    @TableField(value = "busi_type")
    @Column(comment = "业务类型，1，一次绩效，2，科室成本", isNull = false, defaultValue = "1", type = MySqlTypeConstant.VARCHAR, length = 20)
    @Index
    private String busiType= "1";

    @TableField(value = "account_user_code",updateStrategy = FieldStrategy.ALWAYS)
    @Column(comment="科室人员类型", type = MySqlTypeConstant.VARCHAR, length = 500)
    private String accountUserCode;

    @TableField(value = "group_code")
    @Column(comment="分组code", type = MySqlTypeConstant.VARCHAR, length = 500)
    private String groupCode;

    @TableField(value = "account_group")
    @Column(comment="核算组别", type = MySqlTypeConstant.VARCHAR, length = 100)
    private String accountGroup;

    @TableField(value = "unit_code")
    @Column(comment="科室编码", type = MySqlTypeConstant.VARCHAR, length = 100)
    private String unitCode;

    @TableField(value = "dept_type")
    @Column(comment="科别  1门诊 2病区", type = MySqlTypeConstant.VARCHAR, length = 10)
    private String deptType;

    @TableField(value = "factor",updateStrategy = FieldStrategy.IGNORED)
    @Column(comment = "科室默认系数" ,type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal factor;
}