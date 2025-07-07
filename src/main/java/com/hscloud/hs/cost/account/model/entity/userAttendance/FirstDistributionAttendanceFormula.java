package com.hscloud.hs.cost.account.model.entity.userAttendance;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.hscloud.hs.cost.account.model.dto.userAttendance.FirstDistributionAccountFormulaDto;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * <p>
 * 一次分配考勤公式配置表
 * </p>
 *
 * @author JC
 * @since 2023-11-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("first_distribution_attendance_formula")
@Schema(description = "一次分配考勤公式配置表")
public class FirstDistributionAttendanceFormula extends BaseEntity<FirstDistributionAttendanceFormula> {

    private static final long serialVersionUID = 1L;

    @Column(comment = "核算周期")
    @Schema(description = "核算周期")
    private String dt;

    @Column(comment = "方案分配表id")
    @Schema(description = "方案分配表id")
    private Long planId;

    @Column(comment = "方案分配名")
    @Schema(description = "方案分配名")
    private String planName;

    @Column(comment = "出勤天数公式")
    @Schema(description = "出勤天数公式")
    private String attendanceFormula;

    @Column(comment = "保留小数")
    @Schema(description = "保留小数")
    private Integer reservedDecimal;

    @Column(comment = "进位规则")
    @Schema(description = "进位规则")
    private String carryRule;

    @Column(comment = "科室单元", type = MySqlTypeConstant.TEXT)
    @Schema(description = "科室单元")
    private String unitName;

    @Column(comment = "科室单元id", type = MySqlTypeConstant.TEXT)
    @Schema(description = "科室单元id")
    private String unitId;

    @Column(comment = "类型：'1'所有科室单元'2'制定科室单元", defaultValue = "1")
    @Schema(description = "类型：'1'所有科室单元'2'制定科室单元")
    private String formulaType;

    @Column(comment = "描述")
    @Schema(description = "描述")
    private String description;

    @Column(comment = "")
    private String busiType;

    @TableField(exist = false)
    @Schema(description = "参数列表")
    private List<FirstDistributionAccountFormulaDto> paramList;

}
