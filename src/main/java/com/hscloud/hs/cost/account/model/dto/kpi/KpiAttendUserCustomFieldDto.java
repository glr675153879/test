package com.hscloud.hs.cost.account.model.dto.kpi;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class KpiAttendUserCustomFieldDto {
    private String name;
    private BigDecimal num;
    private Long id;

    public KpiAttendUserCustomFieldDto() {
    }

    public KpiAttendUserCustomFieldDto(String name, Long id) {
        this.name = name;
        this.id = id;
    }

    public KpiAttendUserCustomFieldDto(String name, BigDecimal num, Long id) {
        this.name = name;
        this.num = num;
        this.id = id;
    }
}
