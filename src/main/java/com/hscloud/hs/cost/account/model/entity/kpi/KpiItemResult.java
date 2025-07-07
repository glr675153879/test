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
* 核算项结果集备份Model
* @author you
* @since 2024-09-13
*/

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "核算项结果集备份")
@TableName("kpi_item_result")
public class KpiItemResult extends Model<KpiItemResult>{

    @TableId(value = "id", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;

    @TableField(value = "period")
    @Column(comment="周期", type = MySqlTypeConstant.INT , isNull = false )
    private Long period;

    @TableField(value = "code")
    @Column(comment="code 用56转 ，带上前缀，核算项 X_ ，分摊指标 F_ ，核算指标 Z_", type = MySqlTypeConstant.VARCHAR, length = 50 , isNull = false )
    private String code;

    @TableField(value = "busi_code")
    @Column(comment="业务唯一id", type = MySqlTypeConstant.VARCHAR, length = 100)
    private String busiCode;

    @TableField(value = "dept_id", updateStrategy = FieldStrategy.ALWAYS)
    @Column(comment = "科室", type = MySqlTypeConstant.BIGINT)
    private Long deptId;

    @TableField(value = "user_id")
    @Column(comment="人", type = MySqlTypeConstant.BIGINT)
    private Long userId;

    @TableField(value = "imputation_dept_id")
    @Column(comment="归集科室", type = MySqlTypeConstant.BIGINT)
    private Long imputationDeptId;

    @TableField(value = "value")
    @Column(comment="值",type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal value;

    @TableField(value = "source_dept")
    @Column(comment="数据发生科室", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String sourceDept;

    @TableField(value = "zdys")
    @Column(comment="主刀医生", type = MySqlTypeConstant.BIGINT)
    private Long zdys;

    @TableField(value = "brks")
    @Column(comment="病人科室", type = MySqlTypeConstant.BIGINT)
    private Long brks;

    @TableField(value = "kzys")
    @Column(comment="开嘱医生", type = MySqlTypeConstant.BIGINT)
    private Long kzys;

    @TableField(value = "mate_flag")
    @Column(comment="是否需要匹配", type = MySqlTypeConstant.CHAR, length = 1  , isNull = false )
    private String mateFlag;

    @TableField(value = "created_date")
    @Column(comment="创建时间", type = MySqlTypeConstant.DATETIME)
    private Date createdDate;

    @TableField(value = "tenant_id")
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long tenantId;

    @TableField(value = "ward")
    @Column(comment="病区", type = MySqlTypeConstant.BIGINT)
    private Long ward;

    @TableField(value = "zdysks")
    @Column(comment="主刀医生科室", type = MySqlTypeConstant.BIGINT)
    private Long zdysks;

    @TableField(value = "kzysks")
    @Column(comment="开医嘱医生科室", type = MySqlTypeConstant.BIGINT)
    private Long kzysks;

    @TableField(value = "kzyh")
    @Column(comment="开医嘱医生/护士", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String kzyh;

    @TableField(value = "busi_type")
    @Column(comment = "业务类型，1，一次绩效，2，科室成本", isNull = false, defaultValue = "1", type = MySqlTypeConstant.VARCHAR, length = 20)
    @Index
    private String busiType= "1";

    @TableField(value = "brbq")
    @Column(comment="病人病区", type = MySqlTypeConstant.BIGINT)
    private Long brbq;

    @TableField(value = "project_id")
    @Column(comment="项目id", type = MySqlTypeConstant.BIGINT)
    private Long projectId;

    @TableField(value = "ghkb")
    @Column(comment="挂号科别", type = MySqlTypeConstant.VARCHAR)
    private String ghkb;
}