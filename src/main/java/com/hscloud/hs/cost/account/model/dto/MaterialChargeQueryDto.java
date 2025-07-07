package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 物资收费
 * @author  lian
 * @date  2024/6/2 15:06
 *
 */

@Data
@Schema(description = "物资收费")
public class MaterialChargeQueryDto extends PageDto {

    @Schema(description = "物资名称")
    private String resourceName;

    @Schema(description = "仓库名称")
    private String storeName;

    @Schema(description = "是否收费 N 否 Y 是")
    private String isCharge;

    @Schema(description = "匹配类型 1：已匹配 2：未匹配 ")
    private String matchType;

    @Schema(description = "匹配类型 0：启用 1：停用 ")
    private String status;

}

