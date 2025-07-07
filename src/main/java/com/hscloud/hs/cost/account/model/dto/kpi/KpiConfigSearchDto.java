package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Classname KpiConfigSearchDto
 * @Description TODO
 * @Date 2024/11/14 17:27
 * @Created by sch
 */
@Data
public class KpiConfigSearchDto {


    private Long id;

    @Schema(description = "1默认 2科室" )
    private String type="1";

    private Long period;
}
