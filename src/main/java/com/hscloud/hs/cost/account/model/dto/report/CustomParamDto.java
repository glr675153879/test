package com.hscloud.hs.cost.account.model.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 页面查询参数
 *
 * @author : [pc]
 * @version : [v1.0]
 * @createTime : [2024/4/29 10:35]
 */
@Data
@Schema(description = "参数")
public class CustomParamDto {

    @Schema(description = "code")
    private String code;

    @Schema(description = "value")
    private Object value;

    @Schema(description = "操作符 like, =, !=, >, <, >=, <=, in")
    private String operator = "like";

}
