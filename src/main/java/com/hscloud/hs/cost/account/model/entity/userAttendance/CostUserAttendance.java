package com.hscloud.hs.cost.account.model.entity.userAttendance;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.hscloud.hs.cost.account.model.dto.userAttendance.AccountUnitDto;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import com.hscloud.hs.cost.account.model.vo.userAttendance.CustomFieldVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * 人员考勤表
 *
 * @JC
 * @since 2024-05-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("cost_user_attendance")
@Schema(description = "人员考勤表")
public class CostUserAttendance extends BaseEntity<CostUserAttendance> {

    private static final long serialVersionUID = 1L;

    @Column(comment = "年月")
    @Schema(description = "时间id-年月")
    private String dt;

    @Column(comment = "员工工号")
    @Schema(description = "员工工号")
    private String empId;

    @Column(comment = "员工姓名")
    @Schema(description = "员工姓名")
    private String empName;

    @Column(comment = "考勤组")
    @Schema(description = "考勤组")
    private String attendanceGroup;

    @Column(comment = "人员类型")
    @Schema(description = "人员类型")
    private String userType;

    @Column(comment = "职务")
    @Schema(description = "职务")
    private String dutiesName;

    @Column(comment = "核算组别")
    @Schema(description = "核算组别")
    private String accountGroup;

    @Column(comment = "职称")
    @Schema(description = "职称")
    private String titles;

    @Column(comment = "科室单元")
    @Schema(description = "科室单元")
    private String accountUnit;

    @Column(comment = "科室编码")
    @Schema(description = "科室编码")
    private String deptCode;

    @Column(comment = "科室名称")
    @Schema(description = "科室名称")
    private String deptName;

    @Column(comment = "出勤次数")
    @Schema(description = "出勤次数")
    private String attendCount;

    @Column(comment = "出勤系数", decimalLength = 6)
    @Schema(description = "出勤系数")
    private BigDecimal attendRate;

    @Column(comment = "在册系数", decimalLength = 6, defaultValue = "0.000000")
    @Schema(description = "在册系数")
    private BigDecimal registeredRate;

    @Column(comment = "工作性质")
    @Schema(description = "工作性质")
    private String jobNature;

    @Column(comment = "实际出勤天数", decimalLength = 6, defaultValue = "0.000000")
    @Schema(description = "实际出勤天数")
    private BigDecimal attendDays;

    @Column(comment = "岗位")
    @Schema(description = "岗位")
    private String post;

    @Column(comment = "是否拿奖金 0-否 1-是", defaultValue = "0")
    @Schema(description = "是否拿奖金 0-否 1-是")
    private String reward;

    @Column(comment = "奖金系数", decimalLength = 6)
    @Schema(description = "奖金系数")
    private BigDecimal rewardIndex;

    @Column(comment = "不拿奖金原因")
    @Schema(description = "不拿奖金原因")
    private String noRewardReason;

    @Column(comment = "当前考勤组所在天数", decimalLength = 6)
    @Schema(description = "当前考勤组所在天数")
    private BigDecimal attendanceGroupDays;

    @Column(comment = "一次性绩效出勤天数", decimalLength = 6)
    @Schema(description = "一次性绩效出勤天数")
    private BigDecimal oneKpiAttendDays;

    @Column(comment = "一次性绩效出勤系数", decimalLength = 6)
    @Schema(description = "一次性绩效出勤系数")
    private BigDecimal oneKpiAttendRate;

    @Column(comment = "锁定标记 1:锁定0:未锁定", defaultValue = "0")
    @Schema(description = "锁定标记 1:锁定0:未锁定")
    private String isLocked;

    @Column(comment = "编辑标记 1:已编辑0:未编辑")
    @Schema(description = "编辑标记 1:已编辑0:未编辑")
    private String isEdited;

    @Column(comment = "中治室", decimalLength = 6)
    @Schema(description = "中治室")
    private BigDecimal treatRoomDays;

    @Column(comment = "自定义字段", type = MySqlTypeConstant.TEXT)
    @Schema(description = "自定义字段")
    String customFields;

    @Column(comment = "原始自定义字段", type = MySqlTypeConstant.TEXT)
    @Schema(description = "原始自定义字段")
    String originCustomFields;

    @TableField(exist = false)
    private List<CustomFieldVO> customFieldList;

    @TableField(exist = false)
    private List<AccountUnitDto> accountUnits;

}
