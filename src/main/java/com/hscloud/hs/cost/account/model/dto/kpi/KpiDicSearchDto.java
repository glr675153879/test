package com.hscloud.hs.cost.account.model.dto.kpi;

import com.baomidou.mybatisplus.annotation.TableField;
import com.hscloud.hs.cost.account.model.dto.PageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Classname KpiDicSearchDto
 * @Description TODO
 * @Date 2025/4/17 10:44
 * @Created by sch
 */
@Data
public class KpiDicSearchDto extends PageDto {


    @Schema(description = "字典类型")
    private String dictType;

    /**
     * 描述
     */
    @Schema(description = "字典描述")
    private String description;
}
