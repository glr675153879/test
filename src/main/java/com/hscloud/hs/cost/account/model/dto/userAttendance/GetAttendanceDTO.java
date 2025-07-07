package com.hscloud.hs.cost.account.model.dto.userAttendance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class GetAttendanceDTO {
    @JsonProperty("branch_code")
    private String branchCode;
    @JsonProperty("id")
    private String id;
    @JsonProperty("attend_year")
    private String attendYear;
    @JsonProperty("attend_month")
    private String attendMonth;
    @JsonProperty("job_number")
    private String jobNumber;
    @JsonProperty("name")
    private String name;
    @JsonProperty("job_name")
    private String jobName;
    @JsonProperty("title_name")
    private String titleName;
    @JsonProperty("work_nature_nm")
    private String workNatureNm;
    @JsonProperty("post_name")
    private String postName;
    @JsonProperty("reward_rmk")
    private String rewardRmk;
    @JsonProperty("reward_rate")
    private String rewardRate;
    @JsonProperty("organization_name")
    private String organizationName;
    @JsonProperty("group_name")
    private String groupName;
    @JsonProperty("tab_info")
    private String tabInfo;
    @JsonProperty("days")
    private String days;
}
