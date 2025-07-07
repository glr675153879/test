package com.hscloud.hs.cost.account.model.dto;

import com.hscloud.hs.cost.account.model.pojo.ValidatorType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author Admin
 */
@Data
@Schema(description = "sql配置校验")
@EqualsAndHashCode(callSuper = true)
public class SqlValidatorDto extends ValidatorType {

    @Schema(description = "sql")
    @NotBlank(message = "sql不能为空")
    private String sql;

    @Schema(description = "参数")
    @NotNull(message = "参数不能为空")
    private List<SqlValidatorParam> params;

    @Schema(description = "进位规则")
    private String carryRule;

    @Schema(description = "保留小数位数")
    private Integer retainDecimal;

    @Data
    @Schema(description = "sql配置校验参数")
    public static class SqlValidatorParam implements Serializable {

        @Schema(description = "参数类型 date string")
        private String type;

        @Schema(description = "参数key")
        private String key;

        @Schema(description = "参数值")
        private String value;

    }
}
