package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Classname ValueOrSystem
 * @Description TODO
 * @Date 2025/4/28 09:13
 * @Created by sch
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValueOrSystem {

    @Schema(description = "补贴value ")
    private BigDecimal value;

    private String isSystem;



}
