package com.hscloud.hs.cost.account.model.dto;

import com.hscloud.hs.cost.account.model.dto.SecondDistributionAccountIndexInfoDto;
import com.hscloud.hs.cost.account.model.dto.SecondDistributionPlanConfigFormulaDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author banana
 * @create 2023-11-28 17:44
 */
@Data
public class saveDistributionAccountPlanDto {

    @Schema(description = "科室单元id")
    @NotNull(message = "科室单元id不能为空")
    private Long unitId;

    @Schema(description = "核算指标配置信息")
    @NotNull(message = "核算指标配置信息不能为空")
    private List<SecondDistributionAccountIndexInfoDto> accountIndexInfoList = new ArrayList<>();

    @Schema(description = "总分配公式")
    @NotNull(message = "总分配公式不能为空")
    private SecondDistributionPlanConfigFormulaDto configFormula;
}
