package com.hscloud.hs.cost.account.model.dto.kpi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
* 指标Model
* @author you
* @since 2024-09-09
*/

@Data
@Schema(description = "批量启用停用入参")
public class KpiIndexBatchEnableDto {

    @Schema(description = "")
    private String ids;

    @Schema(description = "状态：0：启用  1:停用")
    private String status;

}