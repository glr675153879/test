package com.hscloud.hs.cost.account.model.entity.kpi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
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

/**
 * 科室映射;
 * @author : http://www.chiner.pro
 * @date : 2024-12-26
 */
@Data
@Accessors(chain = true)
@TenantTable
@Schema(description = "科室映射")
@TableName("kpi_dept_map")
public class KpiDeptMap extends Model<KpiDeptMap>{
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @TableField("ID")
    @Schema(description = "")
    @Column(comment = "")
    private Long id ;
    @TableField("BEGIN_DATE")
    @Schema(description = "")
    @Column(comment = "",isNull = false ,type = MySqlTypeConstant.INT)
    private String beginDate ;
    @TableField("END_DATE")
    @Schema(description = "")
    @Column(comment = "",isNull = false ,type = MySqlTypeConstant.INT)
    private String endDate ;
    @TableField("FROM_DEPT_ID")
    @Schema(description = "映射前科室")
    @Column(comment = "映射前科室",isNull = false)
    private Long fromDeptId ;
    @TableField("TO_DEPT_ID")
    @Schema(description = "映射后科室")
    @Column(comment = "映射后科室",isNull = false)
    private Long toDeptId ;
    @TableField(exist = false)
    private String toDeptName ;
    @TableField(exist = false)
    private String fromDeptName ;
    @TableField(value = "tenant_id")
    @Column(comment="", type = MySqlTypeConstant.BIGINT)
    private Long tenantId;
    @TableField(value = "period")
    @Column(comment="周期", type = MySqlTypeConstant.INT , isNull = false )
    private Long period;
}