package com.hscloud.hs.cost.account.model.vo.kpi;

import lombok.Data;

@Data
public class KpiCalculateGroupByVo {
    public KpiCalculateGroupByVo(Long userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    public KpiCalculateGroupByVo() {

    }

    private Long userId;
    private String userName;
    private Long deptId;
    private String deptName;
    private String code;
    private String name;

    public KpiCalculateGroupByVo(Long userId, String userName,Long deptId, String deptName,String code,String name) {
        this.userId = userId;
        this.userName = userName;
        this.deptId = deptId;
        this.deptName = deptName;
        this.code=code;
        this.name=name;
    }
}
