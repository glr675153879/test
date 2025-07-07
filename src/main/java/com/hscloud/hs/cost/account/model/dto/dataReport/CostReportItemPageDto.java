package com.hscloud.hs.cost.account.model.dto.dataReport;

import lombok.Data;

import javax.persistence.Entity;
import java.time.LocalDateTime;

@Data
@Entity
public class CostReportItemPageDto {

    private Long id;

    private String name;

    private String measureUnit;

    private String reportType;

    private String dataType;

    private String description;

    private String isDeptDistinguished;

    private String delFlag;

    private String status;

    private String createBy;

    private String updateBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long tenantId;

    private String isUsed;

    private String type;

}
