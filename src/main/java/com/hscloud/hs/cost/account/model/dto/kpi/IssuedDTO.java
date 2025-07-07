package com.hscloud.hs.cost.account.model.dto.kpi;

import lombok.Data;

@Data
public class IssuedDTO {
    private Long id;
    //= Y时说明当前套餐中不存在二次分配，即后续流程中二次分配标记需要手动赋值，即后端在锁定时同时将下发相关动作标记打成Y
    private String noSecondAssign;
}
