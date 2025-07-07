package com.hscloud.hs.cost.account.model.vo;

import com.hscloud.hs.cost.account.model.pojo.SecondDistributionFormula;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author banana
 * @create 2023-11-27 19:29
 */
@Data
public class SecondDistributionAccountIndexInfoVo {

    @Schema(description = "核算指标id")
    private Long id;

    @Schema(description = "标识 1管理类型  2公式类型")
    private String tag;

    @Schema(description = "核算指标")
    private String accountIndex;

    @Schema(description = "进位规则")
    private String carryRule;

    @Schema(description = "指标保留小数")
    private Integer reservedDecimal;

    @Schema(description = "管理岗位信息")
    private List<ManagementItemVo> managementInfos = new ArrayList<>();

    @Schema(description = "指标公式")
    private SecondDistributionFormula otherFormula = new SecondDistributionFormula();

}
