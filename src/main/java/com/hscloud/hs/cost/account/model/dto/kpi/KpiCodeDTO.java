package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class KpiCodeDTO {
    @Schema(description = "id不为空就修改")
    private Long id;
    @Schema(description = "接口code")
    private String code;
    @Schema(description = "接口名称")
    private String name;
    @Schema(description = "口径")
    private String caliber;
    @Schema(description = "类型1自定义报表 2归集报表")
    private String type;

    @Schema(description = "人员分组,分割")
    private String userGroup;
    private List<KpiCodeDetailDTO> list = new ArrayList<>();
}
