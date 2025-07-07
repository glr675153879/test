package com.hscloud.hs.cost.account.model.entity.kpi;

import com.baomidou.mybatisplus.annotation.*;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
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

/**
 * 配置Model
 *
 * @author you
 * @since 2024-09-13
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "配置表")
@TableName("kpi_config")
public class KpiConfig extends Model<KpiConfig> {

    @TableId(value = "id", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment = "", type = MySqlTypeConstant.BIGINT, isNull = false)
    private Long id;

    @TableField(value = "period")
    @Column(comment = "周期", type = MySqlTypeConstant.INT, isNull = false)
    private Long period;

    @TableField(value = "user_flag")
    @Column(comment = "人员锁定标记Y/N 绩效", type = MySqlTypeConstant.CHAR, length = 1)
    private String userFlag;

    @TableField(value = "user_flag_ks")
    @Column(comment = "人员锁定标记Y/N 科室", type = MySqlTypeConstant.CHAR, length = 1)
    private String userFlagKs;

    @TableField(value = "attendance_update_date")
    @Column(comment = "人员锁定时间-一次绩效", type = MySqlTypeConstant.DATETIME)
    private Date attendanceUpdateDate;

    @TableField(value = "attendance_update_date_ks")
    @Column(comment = "人员锁定时间-科室成本", type = MySqlTypeConstant.DATETIME)
    private Date attendanceUpdateDateKs;

    @TableField(value = "index_flag")
    @Column(comment = "指标项重抽标记0,全量抽取中，1,全量抽取完成，9抽取异常结束", type = MySqlTypeConstant.CHAR, length = 1)
    private String indexFlag;
    @TableField(value = "index_flag_ks")
    @Column(comment = "指标项重抽标记0,全量抽取中，1,全量抽取完成，9抽取异常结束", type = MySqlTypeConstant.CHAR, length = 1)
    private String indexFlagKs;

    @TableField(value = "index_update_date")
    @Column(comment = "指标项最近抽取时间", type = MySqlTypeConstant.DATETIME)
    private Date indexUpdateDate;

    @TableField(value = "issued_flag")
    @Column(comment = "是否下发 Y/N", type = MySqlTypeConstant.CHAR, length = 1)
    private String issuedFlag;

    @TableField(value = "issued_date", updateStrategy = FieldStrategy.IGNORED)
    @Column(comment = "下发时间", type = MySqlTypeConstant.DATETIME)
    private Date issuedDate;

    @TableField(value = "task_child_id", updateStrategy = FieldStrategy.IGNORED)
    @Column(comment = "下发子任务id", type = MySqlTypeConstant.BIGINT)
    private Long taskChildId;

    @TableField(value = "imputation_flag")
    @Column(comment = "归集重算标记YN", type = MySqlTypeConstant.CHAR, length = 1)
    private String imputationFlag;

    @TableField(value = "imputation_date")
    @Column(comment = "归集最近重算时间", type = MySqlTypeConstant.DATETIME)
    private Date imputationDate;

    @TableField(value = "tenant_id")
    @Column(comment = "", type = MySqlTypeConstant.BIGINT)
    private Long tenantId;

    @TableField(value = "default_flag")
    @Column(comment = "是否默认周期 Y/N", type = MySqlTypeConstant.CHAR, length = 1)
    private String defaultFlag;

    @TableField(value = "default_ks_flag")
    @Column(comment = "是否默认周期 Y/N 科室", type = MySqlTypeConstant.CHAR, length = 1)
    private String defaultKsFlag;

    @TableField(value = "equivalent_flag")
    @Column(comment = "当量锁定标记 Y/N", type = MySqlTypeConstant.CHAR, length = 1)
    private String equivalentFlag;

    @TableField(value = "equivalent_update_date", updateStrategy = FieldStrategy.ALWAYS)
    @Column(comment = "当量锁定时间", type = MySqlTypeConstant.DATETIME)
    private Date equivalentUpdateDate;

    @TableField(value = "equivalent_index_flag")
    @Column(comment = "当量指标项抽取 0-抽取中，1-抽取完成，9-抽取异常结束", type = MySqlTypeConstant.CHAR, length = 1)
    private String equivalentIndexFlag;

    @TableField(value = "equivalent_index_update_date")
    @Column(comment = "当量指标项最近抽取时间", type = MySqlTypeConstant.DATETIME)
    private Date equivalentIndexUpdateDate;

    @TableField(value = "non_equivalent_index_update_date")
    @Column(comment = "非当量核算项最近抽取时间", type = MySqlTypeConstant.DATETIME)
    private Date nonEquivalentIndexUpdateDate;

    @TableField(value = "equivalent_price", updateStrategy = FieldStrategy.ALWAYS)
    @Column(comment = "当量单价", type = MySqlTypeConstant.DECIMAL, length = 10, decimalLength = 2)
    private BigDecimal equivalentPrice;

    @TableField(value = "sign_flag")
    @Column(comment = "绩效签发锁定标记 Y/N", type = MySqlTypeConstant.CHAR, length = 1)
    private String signFlag;
}