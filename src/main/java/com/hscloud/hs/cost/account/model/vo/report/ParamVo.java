package com.hscloud.hs.cost.account.model.vo.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author : [pc]
 * @version : [v1.0]
 * @createTime : [2024/4/29 11:30]
 */
@Data
@Schema(description = "参数")
public class ParamVo {

    @Schema(description = "code")
    private String code;

    @Schema(description = "value")
    private Object value;
}
