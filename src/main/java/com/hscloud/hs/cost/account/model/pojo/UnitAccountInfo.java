package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Admin
 */
@Data
@Schema(description = "核算单元核算信息")
public class UnitAccountInfo {

    @Schema(description = "核算单元id")
    private Long unitId;

    @Schema(description = "核算单元名称")
    private String unitName;

    @Schema(description = "核算单元分组id")
    private String groupId;

    @Schema(description = "总核算值")
    private BigDecimal totalCost;

    @Schema(description = "核算单元分组名称")
    private String groupName;

    @Schema(description = "核算信息")
    private List<CalculateInfo> calculateInfo;

}
