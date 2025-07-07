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

import java.math.BigDecimal;
import java.util.Date;

/**
* 配置Model
* @author you
* @since 2024-09-13
*/

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "字典系数表")
@TableName("kpi_coefficient")
public class KpiCoefficient extends Model<KpiCoefficient>{

    @TableId(value = "id", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;

    @TableField(value = "dic_type")
    @Column(comment="字典类型", type = MySqlTypeConstant.CHAR, length = 100 )
    private String dicType;

    @TableField(value = "dic_code")
    @Column(comment="字典值", type = MySqlTypeConstant.CHAR, length = 100 )
    private String dicCode;


    @TableField(value = "value")
    @Column(comment="维护系数", type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal value;

    @TableField(value = "tenant_id")
    @Column(comment="", type = MySqlTypeConstant.BIGINT)
    private Long tenantId;
}