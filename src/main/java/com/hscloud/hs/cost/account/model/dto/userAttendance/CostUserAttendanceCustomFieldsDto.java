package com.hscloud.hs.cost.account.model.dto.userAttendance;

import com.hscloud.hs.cost.account.model.entity.userAttendance.CostUserAttendanceCustomFields;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "人员考勤表自定义字段dto")
public class CostUserAttendanceCustomFieldsDto {

    private List<CostUserAttendanceCustomFields> customParams;
    private String dt;
}
