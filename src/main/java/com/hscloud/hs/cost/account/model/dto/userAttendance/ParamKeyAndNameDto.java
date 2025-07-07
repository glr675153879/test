package com.hscloud.hs.cost.account.model.dto.userAttendance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "变动人员处理Dto")
public class ParamKeyAndNameDto {
    @Schema(description = "id")
    private String id;

    @Schema(description = "参数名")
    private String paramName;

    @Schema(description = "编码")
    private String paramKey;
}
