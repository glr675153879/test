package com.hscloud.hs.cost.account.model.entity.kpi;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.Index;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsAutoIncrement;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
* 核算项(cost_account_item)Model
* @author you
* @since 2024-09-13
*/

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "核算项(cost_account_item)")
@TableName("kpi_item_sql")
public class KpiItemSql extends Model<KpiItemSql>{

    @TableId(value = "id", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;

    @TableField(value = "item_name")
    @Column(comment="核算项名称", type = MySqlTypeConstant.VARCHAR, length = 64 , isNull = false )
    private String itemName;

    @TableField(value = "category_code")
    @Column(comment="分组code", type = MySqlTypeConstant.VARCHAR, length = 255 , isNull = false )
    private String categoryCode;

    @TableField(value = "acq_method")
    @Column(comment="采集方式,1,sql,2手工", type = MySqlTypeConstant.CHAR, length = 1  , isNull = false )
    private String acqMethod;

    @TableField(value = "report_id")
    @Column(comment="手工上报项id", type = MySqlTypeConstant.BIGINT)
    private Long reportId;

    @TableField(value = "retain_decimal")
    @Column(comment="指标保留小数", type = MySqlTypeConstant.INT , isNull = false )
    private Integer retainDecimal;

    @TableField(value = "carry_rule")
    @Column(comment="进位规则 1四舍五入 2向上取整 3向下取整", type = MySqlTypeConstant.CHAR, length = 1  , isNull = false )
    private String carryRule;

    @TableField(value = "account_object")
    @Column(comment="出参字段合集", type = MySqlTypeConstant.VARCHAR, length = 2000 , isNull = false )
    private String accountObject;

    @TableField(value = "ext_status")
    @Column(comment="计算状态 0未计算 1计算中 2已完成 9计算异常", type = MySqlTypeConstant.CHAR, length = 1 )
    private String extStatus;

    @TableField(value = "config")
    @Column(comment="配置信息", type = MySqlTypeConstant.TEXT )
    private String config;

    @TableField(value = "status")
    @Column(comment="启停用标记，0启用，1停用", type = MySqlTypeConstant.CHAR, length = 1  , isNull = false,defaultValue = "0")
    private String status;

    @TableField(value = "del_flag",fill = FieldFill.INSERT)
    @TableLogic(value = "0", delval = "1")
    @Column(comment="删除标记，0未删除，1已删除", type = MySqlTypeConstant.CHAR, length = 1,defaultValue = "0")
    private String delFlag;

    @TableField(value = "change_flag")
    @Column(comment="是否需要转科", type = MySqlTypeConstant.CHAR, length = 1 )
    private String changeFlag;

    @TableField(value = "caliber")
    @Column(comment="口径颗粒度 1人2科室3归集4固定值5多条件", type = MySqlTypeConstant.CHAR, length = 1 )
    private String caliber;

    @TableField(value = "check_status")
    @Column(comment="是否校验", type = MySqlTypeConstant.CHAR, length = 1 )
    private String checkStatus;

    @TableField(value = "condition_flag")
    @Column(comment="是否多条件指标", type = MySqlTypeConstant.CHAR, length = 1 )
    private String conditionFlag;

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

    @TableField(value = "beds_flag")
    @Column(comment="是否用于病区借床分摊", type = MySqlTypeConstant.CHAR, length = 1 )
    private String bedsFlag;

    @TableField(value = "description")
    @Column(comment="描述", type = MySqlTypeConstant.VARCHAR, length = 1000)
    private String description;

    @TableField(value = "error_info")
    @Column(comment="错误信息", type = MySqlTypeConstant.TEXT )
    private String errorInfo;

    @TableField(value = "ext_date")
    @Column(comment="计算完成时间", type = MySqlTypeConstant.DATETIME)
    private Date extDate;

    @TableField(value = "ext_template")
    @Column(comment="上一次测试用例", type = MySqlTypeConstant.TEXT )
    private String extTemplate;

    @TableField(value = "code")
    @Column(comment="code 用56转 ，带上前缀，核算项 X_ ，分摊指标 F_ ，核算指标 Z_", type = MySqlTypeConstant.VARCHAR, length = 50 , isNull = false )
    private String code;

    @TableField(value = "busi_type")
    @Column(comment = "业务类型，1，一次绩效，2，科室成本", isNull = false, defaultValue = "1", type = MySqlTypeConstant.VARCHAR, length = 20)
    @Index
    private String busiType= "1";

    @TableField(value = "project_flag")
    @Column(comment="是否项目成本", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String projectFlag;

    @TableField(value = "second_flag")
    @Column(comment="是否用于二次分配", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String secondFlag;

    @TableField(value = "ext_num")
    @Column(comment="计算结果数量", type = MySqlTypeConstant.INT)
    private Integer extNum;
}