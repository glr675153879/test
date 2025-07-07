package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "人员出勤率入参对象")
public class UserAttendanceVo {


    private Long userId;

    private BigDecimal attendance;

}
