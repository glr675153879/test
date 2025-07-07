package com.hscloud.hs.cost.account.model.dto.bi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author : [pc]
 * @version : [v1.0]
 * @createTime : [2024/4/29 10:35]
 */
@Data
@Schema(description = "多科室单月环比报表入参")
public class MultiDeptSingleMonthDataDTO {

    @NotBlank
    @Schema(description = "reportCode")
    private String reportCode;

    @Schema(description = "周期")
    private String accountTime;

    @Schema(description = "核算单元id列表")
    private List<String> accountUnitIds;

    @Schema(description = "需计算环比值的字段key")
    private List<String> qoqKeys;

    @Schema(description = "排序字段")
    private String sortKey;

    @Schema(description = "需要n行数据")
    private Integer limit;

}
