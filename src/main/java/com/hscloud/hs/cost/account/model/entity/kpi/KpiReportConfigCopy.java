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

import java.lang.reflect.Field;
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
@Schema(description = "报表多选配置")
@TableName("kpi_report_config_copy")
//!!!加字段 /copy/reportConfig 备份copy!!!

public class KpiReportConfigCopy extends Model<KpiReportConfigCopy>{

    @TableId(value = "zj", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long zj;

    @TableField(value = "id")
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false)
    private Long id;

    @TableField(value = "`group`")
    @Column(comment="分组", type = MySqlTypeConstant.VARCHAR, length = 100)
    private String group;

    @TableField(value = "establish", updateStrategy = FieldStrategy.ALWAYS)
    @Column(comment="1编内 0编外", type = MySqlTypeConstant.CHAR, length = 1 )
    private String establish;

    @TableField(value = "name")
    @Column(comment="报表名称",  type = MySqlTypeConstant.VARCHAR, length = 200)
    private String name;

    @TableField(value = "caliber")
    @Column(comment="口径颗粒度 1人2科室3人系统4科室系统  5人+科室9全院", type = MySqlTypeConstant.CHAR, length = 1 )
    private String caliber;

    @TableField(value = "field")
    @Column(comment="关联展示字段JSON", type = MySqlTypeConstant.TEXT)
    @Schema(description = "关联展示字段JSON")
    private String field;

    @TableField(value = "`index`")
    @Schema(description="指标选择JSON     [{\"code\":\"z_awl\",\"name\":\"个人四级手术台数\",\"lastMonth\":\"Y\",\"sum\":\"Y\"},{\"code\":\"z_clc\",\"name\":\"管理绩效（科主任/护士长）\",\"lastMonth\":\"Y\",\"sum\":\"Y\"}]")
    @Column(comment="指标选择JSON     [{\"code\":\"z_awl\",\"name\":\"个人四级手术台数\",\"lastMonth\":\"Y\",\"sum\":\"Y\"},{\"code\":\"z_clc\",\"name\":\"管理绩效（科主任/护士长）\",\"lastMonth\":\"Y\",\"sum\":\"Y\"}]", type = MySqlTypeConstant.TEXT)
    private String index;
    @TableField(value = "imp_code")
    @Column(comment="归集code", type = MySqlTypeConstant.VARCHAR, length = 50)
    private String impCode;
    @TableField(value = "imp_type")
    @Column(comment="查看归集类型 1人 2科室 ", type = MySqlTypeConstant.VARCHAR, length = 1)
    private String impType;

    @TableField(value = "`range`")
    @Schema(description = "数据范围JSON 公式里fieldList那个对象")
    @Column(comment="数据范围JSON 公式里fieldList那个对象", type = MySqlTypeConstant.TEXT)
    private String range;

    @TableField(value = "period")
    @Column(comment="周期", type = MySqlTypeConstant.BIGINT)
    private Long period;

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

    @TableField(value = "status")
    @Column(comment="状态：0：启用  1:停用", type = MySqlTypeConstant.CHAR, length = 1 )
    private String status;

    @TableField(value = "task_child_id")
    @Column(comment="子任务id", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long taskChildId;

    @TableField(value = "copy_date")
    @Column(comment="备份时间", type = MySqlTypeConstant.DATETIME , isNull = false )
    private Date copyDate;

    @TableField(value = "type")
    @Column(comment="类型 1一次分配报表 2二次分配报表 3年度报表", type = MySqlTypeConstant.CHAR, length = 1,defaultValue = "1")
    private String type;

    @TableField(value = "report_type")
    @Column(comment="报表类型 1，系统报表，2 动态报表", type = MySqlTypeConstant.CHAR, length = 1,defaultValue = "1")
    private String reportType;

    @TableField(value = "report_code")
    @Column(comment="动态报表编码",  type = MySqlTypeConstant.VARCHAR, length = 200)
    private String reportCode;

    @TableField(value = "seq")
    @Column(comment="", type = MySqlTypeConstant.INT)
    private Long seq;
}