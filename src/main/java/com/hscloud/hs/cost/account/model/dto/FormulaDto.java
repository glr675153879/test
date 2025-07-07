package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Admin
 */
@Schema(description = "公式传参对象")
@Data
public class FormulaDto {

    @Schema(description = "公式表达式 比如var + (var1 * var2 - var3) / 2 % var_x")
    @NotBlank(message = "公式表达式不能为空")
    private String expression;

    /**
     * 总成本公式
     */
    @Schema(description = "公式描述")
    private String costDescription;

    @Schema(description = "公式参数")
    @NotNull(message = "公式参数不能为空")
    @Valid
    private List<FormulaParam> params;

    @Data
    @Schema(description = "公式参数")
    public static class FormulaParam {

        @Schema(description = "配置项名称")
        private String name;

        @Schema(description = "配置项描述")
        private String desc;

        @Schema(description = "核算维度")
        private String dimension;

        @Schema(description = "核算比例描述")
        private String accountProportionDesc;

        @Schema(description = "配置类型 item:核算项 index:核算指标 ")
        @NotBlank(message = "配置类型不能为空")
        private String type;

        @Schema(description = "公式参数对应的key")
        @NotBlank(message = "公式参数对应的key不能为空")
        private String key;

        @Schema(description = "配置项的值 如果是配置项就是CostIndexConfigItemDto对象的id 如果是分摊规则就是CostAllocationRuleConfigItemDto对象的字符串 如果是核算指标就是核算指标的id值")
        @NotBlank(message = "配置项的值不能为空")
        private String value;

    }

}
