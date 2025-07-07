package com.hscloud.hs.cost.account.model.dto.userAttendance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "人员考勤参数表")
public class FirstDistributionAccountFormulaParamDto {
    private Long id;
    private String columnType;
    private String name;
    private String code;
    private String status;
}
