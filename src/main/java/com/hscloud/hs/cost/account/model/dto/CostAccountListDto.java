package com.hscloud.hs.cost.account.model.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author banana
 * @create 2023-09-13 19:18
 */
@Data
@Schema(description = "获取核算列表入参")
public class CostAccountListDto extends PageDto {

    @Schema(description = "核算项id")
    private String costAccountItemId;

    @Schema(description = "核算分组id")
    private String typeGroupId;

    @Schema(description = "核算范围")
    private String accountObject;

    @Schema(description = "状态")
    private String status;
}
