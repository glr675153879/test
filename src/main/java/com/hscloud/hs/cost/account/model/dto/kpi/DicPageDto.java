package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.dto.PageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Classname DicPageInput
 * @Description TODO
 * @Date 2023-04-03 14:36
 * @Created by sch
 */
@Data
public class DicPageDto extends PageDto {

    @Schema(description = "")
    private String appCode;

    @Schema(description = "名称")
    private String name;
}
