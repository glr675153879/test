package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author banana
 * @create 2023-11-27 16:39
 */
@Schema(description = "二次分配公式传参对象")
@Data
public class SecondDistributionFormula {

    @Schema(description = "公式表达式 比如var + (var1 * var2 - var3) / 2 % var_x")
    @NotBlank(message = "公式表达式不能为空")
    private String expression;

    @Schema(description = "公式描述")
    private String costDescription;

    @Schema(description = "公式参数")
    @NotNull(message = "公式参数不能为空")
    private List<SecondDistributionFormula.FormulaParam> params = new ArrayList<SecondDistributionFormula.FormulaParam>();

    @Data
    @Schema(description = "公式参数")
    public static class FormulaParam {

        @Schema(description = "配置项名称")
        @NotBlank(message = "公式参数配置项名称不能为空")
        private String name;

        @Schema(description = "配置项描述")
        private String desc;

        @Schema(description = "配置类型 ")
        @NotBlank(message = "公式参数配置类型不能为空")
        private String type;

        @Schema(description = "公式参数对应的key")
        @NotBlank(message = "公式参数对应的key不能为空")
        private String key;

        @Schema(description = "配置项的值")
        @NotBlank(message = "配置项的值不能为空")
        private String value;

    }

}
