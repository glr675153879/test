package com.hscloud.hs.cost.account.model.entity.kpi;

import com.baomidou.mybatisplus.annotation.*;
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
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "绩效签发 项目管理 （右边表头）备份")
@TableName("kpi_sign_head_copy")
@TenantTable
public class KpiSignHeadCopy extends Model<KpiSignHeadCopy> {

    @TableId(value = "zj", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long zj;

    @TableField(value = "id")
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false)
    private Long id;

    @TableField(value = "name")
    @Column(comment="名称", type = MySqlTypeConstant.VARCHAR, length = 50)
    private String name;

    @TableField(value = "count_type")
    @Column(comment="计算方式 1直接输入 2 计算", type = MySqlTypeConstant.INT, isNull = false )
    private int countType;

    @TableField(value = "price")
    @Column(comment="每数量单位标准",type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal price;

    @TableField(value = "source")
    @Column(comment="1手工上报 2数据采集", type = MySqlTypeConstant.INT)
    private int source;

    @TableField(value = "del_flag")
    @Index
    @Column(comment="是否删除：0：未删除 1：删除", type = MySqlTypeConstant.CHAR, length = 1, defaultValue = "0" )
    private String delFlag;

    @TableField(value = "status")
    @Column(comment="启停用标记，0启用，1停用", type = MySqlTypeConstant.CHAR, length = 1 )
    private String status;

    @TableField(value = "code")
    @Column(comment="核算项", type = MySqlTypeConstant.VARCHAR, length = 50)
    private String code;

    @TableField(value = "code_name")
    @Column(comment="核算项名称", type = MySqlTypeConstant.VARCHAR, length = 100)
    private String codeName;

    @TableField(value = "extend_last_month")
    @Column(comment="继承上月 1是2否", type = MySqlTypeConstant.INT)
    private int extendLastMonth;

    @TableField(value = "seq")
    @Column(comment="排序", type = MySqlTypeConstant.INT)
    private int seq;

    @TableField(value = "period")
    @Column(comment="周期", type = MySqlTypeConstant.INT , isNull = false )
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

    @TableField(value = "last_date")
    @Column(comment="上次重算时间  更新时间大于这个时间的话 列表上重新计算右侧结果", type = MySqlTypeConstant.DATETIME)
    private Date lastDate;

    @TableField(value = "tenant_id")
    @Column(comment="", type = MySqlTypeConstant.BIGINT)
    private Long tenantId;
}
