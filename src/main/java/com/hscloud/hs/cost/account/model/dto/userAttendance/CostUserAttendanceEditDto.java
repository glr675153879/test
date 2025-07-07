package com.hscloud.hs.cost.account.model.dto.userAttendance;

import com.hscloud.hs.cost.account.model.entity.userAttendance.CostUserAttendance;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "变动人员处理Dto")
public class CostUserAttendanceEditDto {

    private String dt;
    private List<CostUserAttendance> customParams;
}
