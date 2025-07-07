package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiechenyu
 * @Description：
 * @date 2024/4/22 18:18
 */
@Data
public class KpiImputationDeptDto {

    @Schema(description = "科室Id")
    private String accountUnit;

    @Schema(description = "科室名称")
    private String accountUnitName;

    @Schema(description = "核算组别中文")
    private String accountGroupName;

    @Schema(description = "核算组别")
    private String accountGroup;

    private String empids;
    private Long seq;

    private List<String> list;

    @Schema(description = "人员")
    private List<AttendanceUserDto> user = new ArrayList<>();


}
