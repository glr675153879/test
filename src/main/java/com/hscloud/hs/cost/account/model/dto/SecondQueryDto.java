package com.hscloud.hs.cost.account.model.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 二次分配查询dto
 */
@Data
@Schema(description = "二次分配查询dto")
@EqualsAndHashCode(callSuper = true)
public class SecondQueryDto extends PageDto {

    @Schema(description = "科室单元id")
    private Long unitId;
}
