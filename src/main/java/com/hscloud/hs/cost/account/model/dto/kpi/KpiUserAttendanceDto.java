package com.hscloud.hs.cost.account.model.dto.kpi;

import com.baomidou.mybatisplus.annotation.TableField;
import com.hscloud.hs.cost.account.model.dto.userAttendance.AccountUnitDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Classname KpiUserAttendanceDto
 * @Description TODO
 * @Date 2024-09-11 9:12
 * @Created by sch
 */
@Data
@Schema(description = "变动人员处理Dto2")
public class KpiUserAttendanceDto {

    private Long id;
    private Long period;
    private String empName;
    private String attendanceGroup;
    private String dutiesName;
    private String titles;
    private Long accountUnit;
    private String accountUnitName;
    private String jobNature;
    private String post;
    private String reward;
    private BigDecimal rewardIndex;
    private String noRewardReason;
    @TableField(exist = false)
    private List<AccountUnitDto> accountUnits;
}
