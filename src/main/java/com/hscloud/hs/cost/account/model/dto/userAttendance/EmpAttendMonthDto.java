package com.hscloud.hs.cost.account.model.dto.userAttendance;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.persistence.Entity;

/**
 * 考勤数据落地表
 *
 * @JC
 * @since 2024-05-29
 */

@Data
@Entity
public class EmpAttendMonthDto {
    @Schema(description = "id")
    @JsonProperty("id")
    private String id;

    @Schema(description = "核算周期")
    @JsonProperty("dt")
    private String dt;

    @Schema(description = "工号")
    @JsonProperty("job_number")
    private String jobNumber;

    @Schema(description = "姓名")
    @JsonProperty("name")
    private String name;

    @Schema(description = "职务")
    @JsonProperty("job_name")
    private String jobName;

    @Schema(description = "职称")
    @JsonProperty("title_name")
    private String titleName;

    @Schema(description = "工作性质")
    @JsonProperty("work_nature_nm")
    private String workNatureNm;

    @Schema(description = "岗位")
    @JsonProperty("post_name")
    private String postName;

    @Schema(description = "是否拿奖金 1是 2否")
    @JsonProperty("reward_rmk")
    private String rewardRmk;

    @Schema(description = "奖金系数")
    @JsonProperty("reward_rate")
    private String rewardRate;

    @Schema(description = "不拿奖金原因")
    @JsonProperty("no_reward_reason")
    private String noRewardReason;

    @Schema(description = "科室名称")
    @JsonProperty("organization_name")
    private String organizationName;

    @Schema(description = "科室代码")
    @JsonProperty("organization_cd")
    private String organizationCd;

    @Schema(description = "上报考勤组")
    @JsonProperty("group_name")
    private String groupName;

    @Schema(description = "考勤组出勤天数")
    @JsonProperty("attend_days")
    private String attendDays;

    @Schema(description = "额外字段")
    @JsonProperty("tab_infos")
    private String tabInfos;

}
