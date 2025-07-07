package com.hscloud.hs.cost.account.model.entity.imputation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @author xiechenyu
 * @Description：
 * @date 2024/4/18 8:56
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "归集科室单元")
@TableName("im_imputation_dept_unit")
public class ImputationDeptUnit extends ImputationBaseEntity<ImputationDeptUnit> {
    private static final long serialVersionUID = 1L;

    @Column(comment = "归集主档ID")
    @Schema(description = "归集主档ID")
    private Long imputationId;

    @Column(comment = "科室单元id")
    @Schema(description = "科室单元id")
    private Long accountUnitId;

    @Column(comment = "科室单元名称")
    @Schema(description = "科室单元名称")
    private String accountUnitName;

    @Column(comment = "核算分组")
    @Schema(description = "核算分组")
    private String accountGroupCode;

    @Column(comment = "人员类型")
    @Schema(description = "人员类型，字典（必选）")
    private String personType;

    @Column(comment = "科室绩效点数", decimalLength = 6, length = 15)
    @Schema(description = "科室绩效点数")
    private BigDecimal deptPerformancePoints;

    @Column(comment = "住院绩效点数", decimalLength = 6, length = 15)
    @Schema(description = "住院绩效点数")
    private BigDecimal hospitalPerformancePoints;

    @Column(comment = "岗位系数", decimalLength = 6, length = 15)
    @Schema(description = "岗位系数")
    private BigDecimal postRate;

    @Column(comment = "绩效参考群体")
    @Schema(description = "绩效参考群体，字典")
    private String referenceGroup;

    @Column(comment = "姓名")
    @Schema(description = "姓名")
    private String userName;

    @Column(comment = "职务")
    @Schema(description = "职务")
    private String postName;

    @Column(comment = "userId")
    @Schema(description = "userId")
    private Long userId;

    @Column(comment = "是否已引入上个月",length = 1)
    @Schema(description = "归集人员ID")
    private String ifLastMonth;

    @Column(comment = "是否已引入上个月",length = 1)
    @Schema(description = "是否纳入总院水电分摊 Y N ")
    private String includedWaterAndEle;

    @Column(comment = "是否纳入鄞州门诊水电分摊",length = 1)
    @Schema(description = "是否纳入鄞州门诊水电分摊 Y N ")
    private String includedYinZhouWaterAndEle;

}
