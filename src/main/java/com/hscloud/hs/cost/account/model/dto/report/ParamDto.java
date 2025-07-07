package com.hscloud.hs.cost.account.model.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author : [pc]
 * @version : [v1.0]
 * @createTime : [2024/4/29 10:35]
 */
@Data
@Schema(description = "参数")
public class ParamDto {

    @Schema(description = "code")
    private String code;

    @Schema(description = "value")
    private Object value;

}
