package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Classname KpiHsUserEditDto
 * @Description TODO
 * @Date 2024-09-24 13:51
 * @Created by sch
 */
@Data
public class KpiHsUserEditDto {


    private Long userId;


    private String userTypeCode;


    @Schema(description = "分组code")
    private String categoryCode;


    private String busiType;


    @Schema(description = "归集科室")
    private String gjks;

    @Schema(description = "职务配置")
    private String zw;
}
