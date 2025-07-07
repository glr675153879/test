package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author Admin
 */
@Data
@Schema(description = "校验结果")
public class ValidatorResultVo {

    @Schema(description = "校验结果")
    private String result;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "执行时间")
    private Integer executeTime;

    @Schema(description = "各配置项/配置指标结果")
    private Map<String, BigDecimal> map;
}
