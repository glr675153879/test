package com.hscloud.hs.cost.account.model.vo.dataReport;

import lombok.Data;

import javax.persistence.Entity;

@Data
@Entity
public class SysDeptVo {
    private Long id;
    private Long weight;
    private String  name;
    private Boolean isLock;
    private Long userSize;
    private String delFlag;
    private Boolean isDept;
    private Boolean selectable;
    private String status;
}
