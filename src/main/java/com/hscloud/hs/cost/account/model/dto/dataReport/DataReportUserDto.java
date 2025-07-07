package com.hscloud.hs.cost.account.model.dto.dataReport;

import lombok.Data;

import javax.persistence.Entity;

@Data
@Entity
public class DataReportUserDto {
    private String id;
    private String name;
}
