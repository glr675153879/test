package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Classname KeyValueDTO
 * @Description TODO
 * @Date 2024-01-04 20:37
 * @Created by sch
 */
@Data
@Accessors(chain = true)
public class KeyUserFactorImportDto {

    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "职务")
    private String office;

    @Schema(description = "职称")
    private String jobTitle;

    @Schema(description = "绩效岗位")
    private String performancePositions;

    @Schema(description = "事业单位岗位")
    private String publicPositions;

    @Schema(description = "用工性质")
    private String employmentNature;

    @Schema(description = "人员类别")
    private String personnelCategory;
}
