package com.hscloud.hs.cost.account.model.vo.kpi;

import lombok.Data;

@Data
public class KpiDeptUserIdVO {
    private Long deptId;
    private String deptName;
    private Long userId;
    private String userName;
    private Long period;
    public KpiDeptUserIdVO(Long deptId,String deptName, Long userId,String userName,Long period) {
        this.deptId = deptId;
        this.deptName = deptName;
        this.userName = userName;
        this.userId = userId;
        this.period = period;
    }
    public KpiDeptUserIdVO(Long deptId,String deptName, Long period) {
        this.deptId = deptId;
        this.period = period;
        this.deptName = deptName;
    }

    public KpiDeptUserIdVO(Long deptId, Long userId) {
        this.deptId = deptId;
        this.userId = userId;
    }
}
