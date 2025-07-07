package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "归集核算科室dto")
public class AccountDepartmentDto {

    @Schema(description = "归集核算科室id")
    private Long id;

    @Schema(description = "归集核算科室名称")
    private String name;

    @Schema(description = "归集核算科室类型")
    private String type;
}
