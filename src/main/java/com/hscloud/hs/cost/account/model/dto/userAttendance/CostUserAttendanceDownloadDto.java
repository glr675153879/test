package com.hscloud.hs.cost.account.model.dto.userAttendance;

import com.baomidou.mybatisplus.annotation.TableField;
import com.hscloud.hs.cost.account.model.vo.userAttendance.CustomFieldVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 人员考勤表下载模版dto
 *
 * @JC
 * @since 2024-05-29
 */
@Data
@Schema(description = "人员考勤表下载模版dto")
public class CostUserAttendanceDownloadDto {

    @Schema(description = "员工姓名")
    private String empName;

    @Schema(description = "考勤组")
    private String attendanceGroup;

    @Schema(description = "科室单元")
    private String accountUnit;

    @Schema(description = "人员类型")
    private String userType;

    @Schema(description = "核算组别")
    private String accountGroup;

    @Schema(description = "职务")
    private String dutiesName;

    @Schema(description = "职称")
    private String titles;

    @Schema(description = "工作性质")
    private String jobNature;

    @Schema(description = "岗位")
    private String post;

    @Schema(description = "是否拿奖金 0-否 1-是")
    private String reward;

    @Schema(description = "不拿奖金原因")
    private String noRewardReason;

    @Schema(description = "奖金系数")
    private String rewardIndex;

    @Schema(description = "当前考勤组所在天数")
    private String attendanceGroupDays;

    @Schema(description = "一次性绩效出勤天数")
    private String oneKpiAttendDays;

    @Schema(description = "实际出勤天数")
    private String attendDays;

    @Schema(description = "出勤次数")
    private String attendCount;

    @Schema(description = "出勤系数")
    private String attendRate;

    @Schema(description = "在册系数")
    private String registeredRate;

    @Schema(description = "一次性绩效出勤系数")
    private String oneKpiAttendRate;

    @Schema(description = "中治室")
    private String treatRoomDays;

    @TableField(exist = false)
    List<CustomFieldVO> customFieldList;
}

