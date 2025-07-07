package com.hscloud.hs.cost.account.model.entity.userAttendance;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 一次分配考勤公式参数
 * </p>
 *
 * @since 2024-05-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("first_distribution_attendance_formula_param")
@Schema(description = "一次分配考勤公式参数")
public class FirstDistributionAccountFormulaParam extends BaseEntity<FirstDistributionAccountFormulaParam> {

    private static final long serialVersionUID = 1L;

    @Column(comment = "方案id")
    @Schema(description = "方案id")
    private Long planId;

    @Column(comment = "公式参数key")
    @Schema(description = "公式参数key")
    private String paramKey;

    @Column(comment = "公式参数名称")
    @Schema(description = "公式参数名称")
    private String paramName;

    @Column(comment = "参数类型 1分配设置 2指标 3数据小组")
    @Schema(description = "参数类型 1分配设置 2指标 3数据小组")
    private String paramType;

    @Column(comment = "参数值")
    @Schema(description = "参数值")
    private String paramValue;

    @Column(comment = "科室单元id")
    @Schema(description = "科室单元id")
    private String unitId;

    @Column(comment = "科室单元名")
    @Schema(description = "科室单元名")
    private String unitName;

}
