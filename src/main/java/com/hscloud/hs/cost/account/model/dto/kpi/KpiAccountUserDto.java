package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Classname KpiAccountUserAddDto
 * @Description TODO
 * @Date 2024-09-12 10:41
 * @Created by sch
 */
@Data
public class KpiAccountUserDto {

    private String empId;
    private String userId;

    private String empName;

    private String userType;

    private String accountUnit;

    private String accountUnitName;

    private String Gjks;

    private String GjksName;

    private Long memberId;

    private String  userTypeCode;

    @Schema(description = "职务")
    private String zw;

    @Schema(description = "职务中文")
    private String zwName;


}
