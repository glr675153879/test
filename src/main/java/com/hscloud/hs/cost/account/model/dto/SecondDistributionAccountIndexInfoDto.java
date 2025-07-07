package com.hscloud.hs.cost.account.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.hscloud.hs.cost.account.model.pojo.SecondDistributionFormula;
import com.hscloud.hs.cost.account.model.vo.ManagementItemVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.format.DecimalStyle;
import java.util.ArrayList;
import java.util.List;

/**
 * @author banana
 * @create 2023-11-28 18:24
 */
@Data
public class SecondDistributionAccountIndexInfoDto {

    @Schema(description = "核算指标id")
    private Long id;

    @Schema(description = "核算指标")
    @NotBlank(message = "核算指标不为空")
    private String accountIndex;

    @Schema(description = "进位规则")
    @NotBlank(message = "进位规则不为空")
    private String carryRule;

    @Schema(description = "指标保留小数")
    @NotNull(message = "指标保留小数不为空")
    private Integer reservedDecimal;

    @Schema(description = "管理岗位信息列表")
    private List<ManagementItemVo> managementInfos = new ArrayList<>();

    @Schema(description = "指标公式")
    private SecondDistributionFormula otherFormula = new SecondDistributionFormula();

}
