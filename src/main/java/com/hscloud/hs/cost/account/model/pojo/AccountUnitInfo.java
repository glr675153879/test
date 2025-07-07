package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author banana
 * @create 2023-09-18 18:54
 */
@Data
@Schema(description = "核算科室单元相关信息")
public class AccountUnitInfo {

    @Schema(description = "科室单元名称")
    private String accountUnit;

    @Schema(description = "核算分组名称")
    private String typeGroup;
}
