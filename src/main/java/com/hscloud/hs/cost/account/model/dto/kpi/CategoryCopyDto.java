package com.hscloud.hs.cost.account.model.dto.kpi;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CategoryCopyDto {
    @NotNull(message = "不可空")
    private Long id;
    @NotBlank(message = "不可空")
    private String name;
}
