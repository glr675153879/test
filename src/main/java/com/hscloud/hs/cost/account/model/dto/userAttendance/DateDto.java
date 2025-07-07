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
public class DateDto {

    @Schema(description = "dt")
    @JsonProperty("dt")
    private String dt;

    @Schema(description = "业务类型，1，一次绩效，2，科室成本")
    private String busiType;

    private String period;
}
