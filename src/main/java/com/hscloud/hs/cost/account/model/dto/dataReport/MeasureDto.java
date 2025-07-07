package com.hscloud.hs.cost.account.model.dto.dataReport;

import lombok.Data;

import javax.persistence.Entity;

@Data
@Entity
public class MeasureDto {
    private Long id;
    private String name;

}
