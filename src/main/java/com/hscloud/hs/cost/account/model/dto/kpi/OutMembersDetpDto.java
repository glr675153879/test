package com.hscloud.hs.cost.account.model.dto.kpi;

import lombok.Data;

import java.util.List;

@Data
public class OutMembersDetpDto {
    private List<Long> out_dept;
    private List<String> out_dept_group;
    private List<Long> out_dept_except;
}
