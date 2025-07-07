package com.hscloud.hs.cost.account.model.entity.kpi;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
* 核算任务(cost_account_task)Model
* @author you
* @since 2024-09-13
*/

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "一次任务报表配置")
@TableName("kpi_report")
public class KpiReport extends Model<KpiReport>{

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;

    @TableField(value = "code")
    @Column(comment="接口编码", type = MySqlTypeConstant.VARCHAR, length = 100)
    private String code;

    @TableField(value = "name")
    @Column(comment="接口名称",  type = MySqlTypeConstant.VARCHAR, length = 100)
    private String name;

    @TableField(value = "caliber")
    @Column(comment="口径颗粒度 1人2科室3归集4固定值", type = MySqlTypeConstant.CHAR, length = 1 )
    private String caliber;

    @TableField(value = "type")
    @Column(comment="类型1自定义报表 2归集报表", type = MySqlTypeConstant.CHAR, length = 1, isNull = false  )
    private String type;

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

    @TableField(value = "user_group")
    @Column(comment="人员分组,分割", type = MySqlTypeConstant.VARCHAR,length = 200)
    @Schema(description = "人员分组,分割")
    private String userGroup;
}