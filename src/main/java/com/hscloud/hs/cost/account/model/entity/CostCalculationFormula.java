package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 核算公式表
 * </p>
 *
 * @author 
 * @since 2023-09-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "核算公式表")
public class CostCalculationFormula extends Model<CostCalculationFormula> {

    private static final long serialVersionUID = 1L;

    @TableId(value = " id", type = IdType.ASSIGN_ID)
    @Schema(description = "指标公式id")
    private Long  id;

    @Schema(description = "配置项id")
    private Long configId;

    @Schema(description = "配置项名称")
    private String configName;

    @Schema(description = "核算对象id")
    private Long accountObjectId;

    @Schema(description = "核算对象名称")
    private String accountObjectName;

    @Schema(description = "科室单元范围id")
    private Long departmentUnitScopeId;

    @Schema(description = "科室单元范围名称")
    private String departmentUnitScopeName;

    @Schema(description = "核算科室单元科室性质id")
    private Long natureOfDepartmentId;

    @Schema(description = "核算科室单元科室性质名称")
    private String natureOfDepartmentName;

    @Schema(description = "核算周期id")
    private Long accountCycleId;

    @Schema(description = "核算周期名称")
    private String accountCycleName;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "修改人")
    private String updateBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    private LocalDateTime updateTime;


}
