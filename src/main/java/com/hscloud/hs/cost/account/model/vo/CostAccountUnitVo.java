package com.hscloud.hs.cost.account.model.vo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分摊科室Vo")
public class CostAccountUnitVo {

    @Schema(description = "科室单元ID")
    private Long accountUnitId;

    @Schema(description = "科室单元名称")
    private String accountUnitName;

    @Schema(description = "科室单元分组")
    private String accountGroupCode;
}
