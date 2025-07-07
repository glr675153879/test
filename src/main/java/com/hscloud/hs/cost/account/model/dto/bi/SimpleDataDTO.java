package com.hscloud.hs.cost.account.model.dto.bi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author : [pc]
 * @version : [v1.0]
 * @createTime : [2024/4/29 10:35]
 */
@Data
@Schema(description = "参数")
public class SimpleDataDTO {

    @NotBlank
    @Schema(description = "reportCode")
    private String reportCode;

    @Schema(description = "周期")
    private String accountTime;

}
