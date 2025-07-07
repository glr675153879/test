package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.dto.PageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Classname KpiAccountUserAddDto
 * @Description TODO
 * @Date 2024-09-12 10:41
 * @Created by sch
 */
@Data
public class KpiChangeUseSearchDto extends PageDto {



    private String period;

    @Schema(description = "1 本月待匹配 2 手动 3系统")
    private Long type;


    @Schema(description = "业务类型，1，一次绩效，2，科室成本")
    private String busiType;

    @Schema(description = "考勤组")
    private String attendanceGroup;
}
