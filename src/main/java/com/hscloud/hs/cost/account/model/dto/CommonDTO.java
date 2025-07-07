package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Schema(description = "基础类型")
public class CommonDTO {

    @Schema(description = "id")
    private String id;

    @Schema(description = "名字")
    private String name;

    @Schema(description = "类型")
    private String type;
}
