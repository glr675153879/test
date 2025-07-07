package com.hscloud.hs.cost.account.model.entity.userAttendance;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 考勤数据落地表
 *
 * @JC
 * @since 2024-05-29
 */


@Data
@Entity
@Table(name = "t_attend_month_view")
public class EmpAttendMonth extends Model<EmpAttendMonth> {

    private static final long serialVersionUID = 1407906288669281233L;
    @Id
    @Schema(description = "统计表主键id")
    private String id;

    @Schema(description = "统计数据归属年份")
    private String attendYear;

    @Schema(description = "统计数据归属月份")
    private String attendMonth;

    @Schema(description = "工号")
    private String jobNumber;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "职务")
    private String jobName;

    @Schema(description = "职称")
    private String titleName;

    @Schema(description = "工作性质")
    private String workNatureNm;

    @Schema(description = "岗位")
    private String postName;

    @Schema(description = "是否拿奖金 1是 2否")
    private String rewardRmk;

    @Schema(description = "奖金系数")
    private String rewardRate;

    @Schema(description = "不拿奖金原因")
    private String noRewardReason;

    @Schema(description = "科室名称")
    private String organizationName;

    @Schema(description = "上报考勤组")
    private String groupName;

    @Schema(description = "考勤天数")
    private String days;

    @Schema(description = "额外字段")
    private String tabInfo;

    @TableField(exist = false)
    private String attendDays;

    @TableField(exist = false)
    @Schema(description = "额外字段")
    private String tabInfos;

}
