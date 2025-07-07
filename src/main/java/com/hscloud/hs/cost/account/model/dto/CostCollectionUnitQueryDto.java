package com.hscloud.hs.cost.account.model.dto;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Schema(description = "归集单元查询对象")
@EqualsAndHashCode(callSuper = true)
public class CostCollectionUnitQueryDto extends PageDto {


    @Schema(description = "归集单元id")
    private Long id;

    @Schema(description = "归集单元名称")
    private String collectionName;

    @Schema(description = "开始日期")
    private LocalDateTime beginTime;

    @Schema(description = "结束日期")
    private LocalDateTime endTime;

}
