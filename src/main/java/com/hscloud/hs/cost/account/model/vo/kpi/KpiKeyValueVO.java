package com.hscloud.hs.cost.account.model.vo.kpi;

import lombok.Data;

import java.util.List;

@Data
public class KpiKeyValueVO {
    public KpiKeyValueVO(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public KpiKeyValueVO() {

    }

    private String key;
    private String value;
    private List<KpiKeyValueVO> indexs;

}
