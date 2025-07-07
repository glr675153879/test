package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @Classname KpiAccountUserAddDto
 * @Description TODO
 * @Date 2024-09-12 10:41
 * @Created by sch
 */
@Data
public class KpiAccountUserAddDto {

//    @Schema(description = "核算表id")
//    private Long attendance_id;

    @Schema(description = "核算人员id")
    private Long userId;

    @Schema(description = "核算人员分组code")
    private String categoryCode;

    @Schema(description = "归集科室")
    private String gjks;

    @Schema(description = "人员类型字典id  dictValue")
    private String userTypeCode;

    private String busiType;

    @Schema(description = "职务")
    private String zw;
}
