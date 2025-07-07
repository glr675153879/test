package com.hscloud.hs.cost.account.model.dto.imputation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author xiechenyu
 * @Description：
 * @date 2024/4/23 16:18
 */
@Data
@Schema(description = "收入归集特殊归集，多个归集指标")
public class SpecialPersonIndexOrUnitDTO {

    @Schema(description = "归集指标id")
    private String id;


    @Schema(description = "归集指标名称")
    private String name;
}
