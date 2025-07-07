package com.hscloud.hs.cost.account.model.vo;

import com.pig4cloud.pigx.admin.api.entity.mapping.MappingBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author pc
 * @date 2025/2/26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "映射数据")
public class MappingBaseVO extends MappingBase {

    private static final long serialVersionUID = 1L;

    @Schema(description = "是否已使用 1已使用 0未使用")
    private String isUsed;

}
