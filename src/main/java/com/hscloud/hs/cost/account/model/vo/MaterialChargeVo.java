package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
/**
 * 物资收费vo
 * @author  lian
 * @date  2024/6/2 16:27
 *
 */

@Data
@Schema(description = "物资收费vo")
public class MaterialChargeVo {

    @Schema(description = "仓库id")
    private String storeId;

    @Schema(description = "仓库名称")
    private String storeName;

    @Schema(description = "物资id")
    private String resourceId;

    @Schema(description = "物资名称")
    private String resourceName;

    @Schema(description = "是否收费")
    private String isCharge;
}
