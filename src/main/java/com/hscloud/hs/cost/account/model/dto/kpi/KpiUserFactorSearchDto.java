package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.dto.PageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Classname KpiCoefficientDto
 * @Description TODO
 * @Date 2024/11/27 13:41
 * @Created by sch
 */
@Data
public class KpiUserFactorSearchDto extends PageDto {

    @Schema(description = "科室名称")
    private String deptName;

    @Schema(description = "name")
    private String name;

    @Schema(description = "职务字典code 暂时不传")
    private String itemCode;

    @Schema(description = "筛选字典名称")
    private String zwName;

    @Schema(description = "筛选字典code")
    private String dictType;



}
