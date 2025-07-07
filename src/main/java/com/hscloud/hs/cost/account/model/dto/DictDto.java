package com.hscloud.hs.cost.account.model.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Admin
 */
@Schema(description = "字典dto")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DictDto {
    @Schema(description = "字典标签")
    private String label;

    @Schema(description = "字典值")
    private String value;
}
