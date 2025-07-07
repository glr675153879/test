package com.hscloud.hs.cost.account.model.dto.kpi;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ForCopyDto {
    @NotNull(message = "不可空")
    private List<Long> id;
    @NotBlank(message = "不可空")
    private String planCode;
    @NotBlank(message = "不可空")
    private String oldPlanCode;
}
