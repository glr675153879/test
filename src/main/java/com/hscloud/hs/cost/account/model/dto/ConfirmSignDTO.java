package com.hscloud.hs.cost.account.model.dto;

import com.hscloud.hs.cost.account.model.entity.kpi.KpiSignHead;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiSignLeft;
import lombok.Data;

import java.util.List;

@Data
public class ConfirmSignDTO {
    /*private List<KpiSignLeft> lefts;
    private List<KpiSignHead> heads;*/

    private Long period;
}
