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

/**
 * @author Administrator
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "医护关系表(cost_account_unit)")
@TableName("kpi_account_relation")
public class KpiAccountRelation extends Model<KpiAccountRelation> {
    @TableId(value = "id", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;

    @TableField(value = "doc_account_id")
    @Column(comment="医生组科室单元id", type = MySqlTypeConstant.BIGINT)
    private Long docAccountId;

    @TableField(value = "nurse_account_id")
    @Column(comment="护士组科室单元id", type = MySqlTypeConstant.BIGINT)
    private Long nurseAccountId;

    @TableField(value = "tenant_id")
    @Column(comment="租户id", type = MySqlTypeConstant.BIGINT)
    private Long tenantId;
}
