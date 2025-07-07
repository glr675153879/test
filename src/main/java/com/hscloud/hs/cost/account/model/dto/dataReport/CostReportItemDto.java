package com.hscloud.hs.cost.account.model.dto.dataReport;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class CostReportItemDto {
    @Getter
    @Setter
    String id;
    String name;
    String dataType;
    String measureUnit;
    String isDeptDistinguished;

    public String getItemName() {
        return name;
    }

    public void setItemName(String itemName) {
        this.name = itemName;
    }
}
