package com.hscloud.hs.cost.account.model.vo.kpi;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class KpiKeyValueVO2 {
    public KpiKeyValueVO2(String key, String name) {
        this.key = key;
        this.name = name;
    }
    public KpiKeyValueVO2(String key, String name,BigDecimal value) {
        this.key = key;
        this.name = name;
        this.value = value;
    }


    public KpiKeyValueVO2() {

    }

    private String key;
    private String name;
    private BigDecimal value;

}
