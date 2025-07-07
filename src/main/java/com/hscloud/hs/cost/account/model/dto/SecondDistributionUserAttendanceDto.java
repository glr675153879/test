package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Schema(description = "人员出勤率入参对象")
public class SecondDistributionUserAttendanceDto {

    @Schema(description = "时间周期")
    @NotNull(message = "时间周期不能为空")
    private String period;

    @Schema(description = "用户id集合")
    @NotNull(message = "用户id集合不能为空")
    private List<Long> userIds;
}
