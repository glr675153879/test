package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Admin
 */
@Data
@Schema(description = "成本核算比例对象")
public class CostAccountProportionVo {

    @Schema(description = "核算比例关联表id")
    private Long id;

    @Schema(description = "核算项id")
    private String itemId;

    @Schema(description = "分组id")
    private String typeGroupId;

    @Schema(description = "分组")
    private String typeGroup;

    @Schema(description = "核算比例")
    private Double proportion;
}
