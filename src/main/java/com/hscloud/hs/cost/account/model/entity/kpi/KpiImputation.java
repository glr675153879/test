package com.hscloud.hs.cost.account.model.entity.kpi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
* 归集Model
* @author you
* @since 2024-09-13
*/

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "归集表")
@TableName("kpi_imputation")
public class KpiImputation extends Model<KpiImputation>{

    @TableId(value = "id", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;

    @TableField(value = "category_code")
    @Column(comment="分组编码", type = MySqlTypeConstant.VARCHAR, length = 255 , isNull = false )
    private String categoryCode;

    @TableField(value = "period")
    @Column(comment="核算周期", type = MySqlTypeConstant.INT , isNull = false )
    private Long period;

    @TableField(value = "dept_id")
    @Column(comment="科室单元id", type = MySqlTypeConstant.BIGINT)
    private Long deptId;

    @TableField(value = "empids")
    @Column(comment="逗号切割存一份 member存一份", type = MySqlTypeConstant.TEXT )
    private String empids;

    @TableField(value = "created_id")
    @Column(comment="创建用户", type = MySqlTypeConstant.BIGINT)
    private Long createdId;

    @TableField(value = "created_date")
    @Column(comment="创建时间", type = MySqlTypeConstant.DATETIME)
    private Date createdDate;

    @TableField(value = "update_id")
    @Column(comment="更新用户", type = MySqlTypeConstant.BIGINT)
    private Long updateId;

    @TableField(value = "update_date")
    @Column(comment="更新时间", type = MySqlTypeConstant.DATETIME)
    private Date updateDate;

    @TableField(value = "tenant_id")
    @Column(comment="", type = MySqlTypeConstant.BIGINT)
    private Long tenantId;

    @TableField(value = "busi_type")
    @Column(comment = "业务类型，1，一次绩效，2，科室成本", isNull = false, defaultValue = "1", type = MySqlTypeConstant.VARCHAR, length = 20)
    @Index
    private String busiType= "1";


}