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
* 配置Model
* @author you
* @since 2024-09-13
*/

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "默认考勤天数")
@TableName("kpi_month_days")
public class KpiMonthDays extends Model<KpiMonthDays>{

    @TableId(value = "id", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;

    @TableField(value = "period")
    @Column(comment="周期", type = MySqlTypeConstant.INT , isNull = false )
    private Long period;


    @TableField(value = "month_days")
    @Column(comment="默认考勤天数", type = MySqlTypeConstant.INT , isNull = false )
    private Long monthDays;

    @TableField(value = "tenant_id")
    @Column(comment = "", type = MySqlTypeConstant.BIGINT)
    private Long tenantId;
}