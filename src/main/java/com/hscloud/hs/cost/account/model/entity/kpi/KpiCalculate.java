package com.hscloud.hs.cost.account.model.entity.kpi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.Index;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsAutoIncrement;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsKey;
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
* 计算结果Model
* @author you
* @since 2024-09-13
*/

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "计算结果")
@TableName("kpi_calculate")
public class KpiCalculate extends Model<KpiCalculate>{

    @TableId(value = "id", type = IdType.AUTO)
    //@IsKey
    @IsAutoIncrement
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;

    @TableField(value = "period")
    //@IsKey
    @Column(comment="周期", type = MySqlTypeConstant.INT , isNull = false )
    private Long period;

    @TableField(value = "plan_child_code")
    @Column(comment="子方案编码", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String planChildCode;

    @TableField(value = "plan_code")
    @Index
    @Column(comment="中方案编码", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String planCode;

    @TableField(value = "task_child_id")
    @Column(comment="子任务id", type = MySqlTypeConstant.BIGINT)
    @Index
    private Long taskChildId;

    @TableField(value = "code")
    @Index
    @Column(comment="指标/分摊代码", type = MySqlTypeConstant.VARCHAR, length = 50)
    private String code;

    @TableField(value = "name")
    @Column(comment="指标/分摊名称", type = MySqlTypeConstant.VARCHAR, length = 50)
    private String name;

    @TableField(value = "value")
    @Column(comment="值",type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal value;

    @TableField(value = "comp_value")
    @Column(comment="对比值",type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal compValue;

    @TableField(value = "dept_id")
    @Column(comment="科室", type = MySqlTypeConstant.BIGINT)
    private Long deptId;

    @TableField(value = "emp_id")
    @Column(comment="人员", type = MySqlTypeConstant.VARCHAR, length = 64)
    private String empId;

    @TableField(value = "establish")
    @Column(comment="1编内 0编外", type = MySqlTypeConstant.CHAR, length = 1 )
    private String establish;

    @TableField(value = "imputation_type")
    @Column(comment="归集类型（0，非归集，1父归集，2，子归集）", type = MySqlTypeConstant.CHAR, length = 1 )
    private String imputationType;

    @TableField(value = "imputation_code")
    @Column(comment="归集编码", type = MySqlTypeConstant.VARCHAR, length = 64)
    private String imputationCode;

    @TableField(value = "result_json")
    @Column(comment="", type = MySqlTypeConstant.TEXT )
    private String resultJson;

    @TableField(value = "created_date")
    @Column(comment="创建时间", type = MySqlTypeConstant.DATETIME)
    private Date createdDate;

    @TableField(value = "tenant_id")
    @Column(comment="", type = MySqlTypeConstant.BIGINT)
    private Long tenantId;

    @TableField(value = "user_id")
    @Column(comment="", type = MySqlTypeConstant.BIGINT)
    private Long userId;

    @TableField(value = "user_name")
    @Column(comment="", type = MySqlTypeConstant.VARCHAR, length = 64)
    private String userName;

    @TableField(value = "dept_name")
    @Column(comment="", type = MySqlTypeConstant.VARCHAR, length = 64)
    private String deptName;

    @TableField(value = "group_name")
    @Column(comment="核算分组名称", type = MySqlTypeConstant.VARCHAR, length = 10)
    private String groupName;

    @TableField(value = "out_name")
    @Column(comment="摊出单元名称", type = MySqlTypeConstant.VARCHAR, length = 200)
    private String outName;

    @TableField(value = "allocation_name")
    @Column(comment="分摊项分摊指标名称", type = MySqlTypeConstant.VARCHAR, length = 500)
    private String allocationName;

    @TableField(value = "allocation_type")
    @Column(comment="分摊类型 1全院分摊 2医护分摊 3病区分摊 5借床分摊 4门诊共用分摊", type = MySqlTypeConstant.VARCHAR, length = 1)
    private String allocationType;

    @TableField(value = "allocation_ratio")
    @Column(comment="分摊比例",type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal allocationRatio;

    @TableField(value = "allocation_value")
    @Column(comment="分摊金额",type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal allocationValue;

    @TableField(value = "seq")
    @Column(comment="计算顺序", type = MySqlTypeConstant.INT)
    private Long seq;

    @TableField(value = "formula_id")
    @Column(comment="公式id", type = MySqlTypeConstant.BIGINT)
    private Long formulaId;

    @TableField(value = "user_type")
    @Column(comment="人员类型", type = MySqlTypeConstant.VARCHAR, length = 50)
    private String userType;

    @TableField(value = "dept_user_type")
    @Column(comment="科室人员类型", type = MySqlTypeConstant.VARCHAR, length = 50)
    private String deptUserType;

    @TableField(value = "user_imp")
    @Column(comment="人员科室归集", type = MySqlTypeConstant.VARCHAR, length = 50)
    private String userImp;

    @TableField(value = "unit_type")
    @Column(comment="核算单元类型", type = MySqlTypeConstant.VARCHAR, length = 50)
    private String unitType;

    @TableField(value = "origin_value")
    @Column(comment="原始值", type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal originValue;

    @TableField(value = "adjust_operation")
    @Column(comment="符号", type = MySqlTypeConstant.CHAR, length = 1 )
    private String adjustOperation;

    @TableField(value = "adjust_value")
    @Column(comment="调整值", type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal adjustValue;

/*    @TableField(value = "ward_income")
    @Column(comment="病区收入(本摊入核算单元)",type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal wardIncome;

    @TableField(value = "ward_income_self")
    @Column(comment="病区收入(本病区)",type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal wardIncomeSelf;*/

    public KpiCalculate(Long userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    public KpiCalculate() {
    }
}