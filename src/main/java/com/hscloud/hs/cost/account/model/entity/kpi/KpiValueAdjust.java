package com.hscloud.hs.cost.account.model.entity.kpi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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

import java.math.BigDecimal;

/**
* 配置Model
* @author you
* @since 2024-09-13
*/

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "每月调整值表")
@TableName("kpi_value_adjust")
public class KpiValueAdjust extends Model<KpiValueAdjust>{

    @TableId(value = "id", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;

    @TableField(value = "type")
    @Column(comment="调整值类型", type = MySqlTypeConstant.CHAR, length = 100 )
    private String type;

    @TableField(value = "code")
    @Column(comment="code", type = MySqlTypeConstant.CHAR, length = 100 )
    private String code;

    @TableField(value = "operation")
    @Column(comment="符号", type = MySqlTypeConstant.CHAR, length = 100 )
    private String operation;

    @TableField(value = "remark")
    @Column(comment="备注", type = MySqlTypeConstant.VARCHAR, length = 200 )
    private String remark;


    @TableField(value = "value")
    @Column(comment="维护系数", type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal value;

    @TableField(value = "tenant_id")
    @Column(comment="", type = MySqlTypeConstant.BIGINT)
    private Long tenantId;

    @TableField(value = "account_unit")
    @Column(comment = "科室单元", type = MySqlTypeConstant.BIGINT)
    private Long accountUnit;

    @TableField(value = "user_id")
    @Column(comment = "用户id", type = MySqlTypeConstant.BIGINT)
    private Long userId;

    @TableField(value = "busi_type")
    @Column(comment = "业务类型，1，一次绩效，2，科室成本", isNull = false, defaultValue = "1", type = MySqlTypeConstant.VARCHAR, length = 20)
    @Index
    private String busiType= "1";

    @TableField(value = "period")
    @Index
    @Column(comment = "周期", type = MySqlTypeConstant.INT, isNull = false)
    private Long period;
}