package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author 小小w
 * @date 2023/10/9 13:42
 */
@Data
@Schema(description = "摊出科室核算单元相关信息")
public class AllocateUnitInfo {
    @Schema(description = "名称描述")
    private String label;

    @Schema(description = "分组信息")
    private String value;

    @Schema(description = "dept group except")
    private String type;
}
