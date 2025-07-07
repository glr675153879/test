package com.hscloud.hs.cost.account.model.dto;

import cn.hutool.json.JSONString;
import io.prometheus.client.Summary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.format.DecimalStyle;

/**
 * @author banana
 * @create 2023-09-20 14:46
 */
@Data
@Schema(description = "数据采集中心入参")
public class CostDataCollectionDto {

    @NotBlank(message = "code不能为空")
    @Schema(description = "报表请求code")
    private String code;

    @Schema(description = "入参内容")
    private Object parameter;
}


