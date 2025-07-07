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
public class KpiAccountUseSearchDto extends PageDto {


    private String empName;

    @Schema(description = "目录code")
    private String categoryCode;

    @Schema(description = "人员类型")
    private String userType;


    private String busiType="1";


}
