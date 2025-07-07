package com.hscloud.hs.cost.account.model.entity.kpi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.Index;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 核算单元(cost_account_unit)Model
 *
 * @author you
 * @since 2024-09-13
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "考勤计算规则(kpi_user_calculation_rule)")
@TableName("kpi_user_calculation_rule")
public class KpiUserCalculationRule extends Model<KpiUserCalculationRule> {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Column(comment = "", type = MySqlTypeConstant.BIGINT, isNull = false)
    private Long id;

    @TableField(value = "rule")
    @Column(comment = "规则json", type = MySqlTypeConstant.VARCHAR, length = 2000)
    private String rule;


    @TableField(value = "status")
    @Column(comment = "启停用标记，0启用，1停用", type = MySqlTypeConstant.CHAR, length = 1, defaultValue = "0")
    private String status;


    @TableField(value = "busi_type")
    @Column(comment = "业务类型，1，一次绩效，2，科室成本", isNull = false, defaultValue = "1", type = MySqlTypeConstant.VARCHAR, length = 20)
    @Index
    private String busiType = "1";


    @TableField(value = "tenant_id")
    @Column(comment = "租户ID", type = MySqlTypeConstant.BIGINT)
    private Long tenantId;

}