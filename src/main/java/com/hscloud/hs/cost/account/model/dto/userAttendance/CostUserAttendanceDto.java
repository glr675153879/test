package com.hscloud.hs.cost.account.model.dto.userAttendance;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "变动人员处理Dto")
public class CostUserAttendanceDto {

    private Long id;
    private String dt;
    private String empName;
    private String attendanceGroup;
    private String dutiesName;
    private String titles;
    private String accountUnit;
    private String jobNature;
    private String post;
    private String reward;
    private BigDecimal rewardIndex;
    private String noRewardReason;
    @TableField(exist = false)
    private List<AccountUnitDto> accountUnits;
}
