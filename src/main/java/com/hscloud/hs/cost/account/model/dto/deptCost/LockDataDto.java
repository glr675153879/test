package com.hscloud.hs.cost.account.model.dto.deptCost;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author banana
 * @create 2024-09-23 19:00
 */
@Data
public class LockDataDto {

    @NotBlank(message = "周期不能为空")
    private String dt;
}
