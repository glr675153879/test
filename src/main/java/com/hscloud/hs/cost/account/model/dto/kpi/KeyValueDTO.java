package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Classname KeyValueDTO
 * @Description TODO
 * @Date 2024-01-04 20:37
 * @Created by sch
 */
@Data
@Accessors(chain = true)
public class KeyValueDTO {

    @Schema(description = "key")
    private String key;

    @Schema(description = "value")
    private String value;
}
