package com.hscloud.hs.cost.account.model.dto.kpi;

import lombok.Data;

/**
 * Author:  Administrator
 * Date:  2025/6/21 15:14
 */
@Data
public class KpiAccountUnitInfo {
    private Long id;
    private String name;
    private String categoryName;
    private String status;
    private String delFlag;
}
