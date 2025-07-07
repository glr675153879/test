package com.hscloud.hs.cost.account.model.entity.kpi;

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
* 核算方案(COST_ACCOUNT_PLAN)Model
* @author you
* @since 2024-09-13
*/

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "核算方案表(COST_ACCOUNT_PLAN)")
@TableName("kpi_account_plan")
public class KpiAccountPlan extends Model<KpiAccountPlan>{

    @TableId(value = "id", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment="", type = MySqlTypeConstant.INT , isNull = false )
    private Long id;

    @TableField(value = "category_code")
    @Column(comment="分组code", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String categoryCode;

    @TableField(value = "plan_code")
    @Column(comment="方案code，按规则生成", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String planCode;

    @TableField(value = "plan_name")
    @Column(comment="方案名称", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String planName;

    @TableField(value = "del_flag")
    @Column(comment="是否删除：0：未删除 1：删除", type = MySqlTypeConstant.CHAR, length = 1 )
    private String delFlag;

    @TableField(value = "status")
    @Column(comment="状态：0：启用  1:停用", type = MySqlTypeConstant.CHAR, length = 1 )
    private String status;

    @TableField(value = "index_code")
    @Column(comment="科室分组", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String indexCode;

    @TableField(exist = false)
    private String indexName;

    @TableField(value = "`range`")
    @Schema(description = "数据范围JSON 公式里fieldList那个对象")
    @Column(comment="数据范围JSON 公式里fieldList那个对象", type = MySqlTypeConstant.TEXT)
    private String range;

    @TableField(exist = false)
    @Schema(description = "指标口径")
    private String caliber;

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


}