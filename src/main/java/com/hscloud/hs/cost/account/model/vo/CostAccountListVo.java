package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author banana
 * @create 2023-09-13 19:22
 */
@Data
@Schema(description = "获取核算信息出参")
public class CostAccountListVo {

    @Schema(description = "核算比例项id")
    private Long id;

    @Schema(description = "核算项")
    private String costAccountItem;

    @Schema(description = "核算范围")
    private String accountObject;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "核算分组名称")
    private String typeGroupName;
}
