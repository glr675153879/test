package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author YJM
 * @date 2023-09-06 09:22
 */
@Data
@Schema(description = "核算单元关联科室人员")
public class CostUnitRelateInfoDto {


    @Schema(description = "科室/人 名称")
    private String name;

    @Schema(description = "关联的科室id/人员id")
    private String id;

    @Schema(description = "关联类型 ")
    private String type;

    @Schema(description = "code")
    private String code;
}
