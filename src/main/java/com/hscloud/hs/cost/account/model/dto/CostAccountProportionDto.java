package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Admin
 */
@Data
@Schema(description = "成本核算比例对象")
public class CostAccountProportionDto {

    @Schema(description = "核算项id")
    @NotNull(message = "核算项id不能为空")
    private Long itemId;

    @Schema(description = "类型 DEPT 自定义科室 USER 自定义人员 默认医护对应科室单元")
    private String type = "KSDYFW010";

    @Schema(description = "业务id列表")
    private List<Long>  bizIds;
}
