package com.hscloud.hs.cost.account.model.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author banana
 * @create 2023-09-11 17:19
 */
@Data
@Schema(description = "获取医护对应组入参")
public class listDocNRelationDto extends PageDto {

    @Schema(description = "医生科室单元名称")
    private String docName;

    @Schema(description = "医生分组")
    private String doc;

    @Schema(description = "护士组科室单元名称")
    private String nurseName;

    @Schema(description = "护士分组")
    private String nurse;
}
