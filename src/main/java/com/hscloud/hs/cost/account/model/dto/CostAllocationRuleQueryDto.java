package com.hscloud.hs.cost.account.model.dto;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.constant.enums.StatisticalPeriodEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分摊规则查询对象")
public class CostAllocationRuleQueryDto extends PageDto {

    @Schema(description = "分摊规则名称")
    private String name;

    @Schema(description = "统计周期")
    private String statisticalCycle;

    @Schema(description = "分摊规则公式")
    private String formula;

    @Schema(description = "状态：0：启用  1:停用")
    private String status;

}
