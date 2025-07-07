package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Admin
 */
@Data
@Schema(description = "核算指标公式")
public class CostFormulaInfo {

    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "配置的key")
    private String key;

    @Schema(description = "类型 item 项目  index 指标")
    private String type;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "配置项的值 如果是配置项就是CostIndexConfigItemDto对象的id 如果是分摊规则就是CostAllocationRuleConfigItemDto对象的字符串 如果是核算指标就是核算指标的id值")
    private Long value;
}
