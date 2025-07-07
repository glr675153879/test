package com.hscloud.hs.cost.account.model.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "核算单元查询对象")
public class CostAccountUnitQueryDtoNew extends PageDto {

    @Schema(description = "科室单元名称")
    private Long id;

    @Schema(description = "科室单元名称")
    private String name;


    @Schema(description = "核算科室、人")
    private String costUnitRelateInfo;

    @Schema(description = "核算分组")
    private String accountGroupCode;

    @Schema(description = "核算类型")
    private String accountTypeCode;

    @Schema(description = "负责人姓名")
    private String responsiblePerson;

/*
    @Schema(description = "负责人dto ")
    private CommonDTO responsiblePerson;*/

    @Schema(description = "启停用标记，0启用，1停用")
    private String status;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
