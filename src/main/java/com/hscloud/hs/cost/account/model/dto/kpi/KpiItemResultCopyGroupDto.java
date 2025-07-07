package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemResultCopy;
import lombok.Data;

import java.util.List;

@Data
public class KpiItemResultCopyGroupDto {
    private String code;
    private String caliber;
    private List<KpiItemResultCopy> list;

    public KpiItemResultCopyGroupDto(String code, List<KpiItemResultCopy> list) {
        this.code = code;
        this.list = list;
    }

    public KpiItemResultCopyGroupDto() {
    }
}
