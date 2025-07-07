package com.hscloud.hs.cost.account.model.vo;

import com.hscloud.hs.cost.account.model.dto.AccountDepartmentDto;
import com.hscloud.hs.cost.account.model.dto.AccountUnitIdAndNameDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "返回归集单元列表对象")
public class CostCollectionUnitVo {

    @Schema(description = "归集单元id")
    private Long id;

    @Schema(description = "归集单元名称")
    private String collectionName;

    @Schema(description = "归集核算科室")
    private AccountDepartmentDto collectionAccountDepartment;

    @Schema(description = "分摊科室单元ids")
    private List<AccountUnitIdAndNameDto> accountUnitIds;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    private LocalDateTime updateTime;

    @Schema(description = "状态：0：启用  1:停用")
    private String status;

}
