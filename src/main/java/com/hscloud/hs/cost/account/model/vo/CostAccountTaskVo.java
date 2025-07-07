package com.hscloud.hs.cost.account.model.vo;

import com.hscloud.hs.cost.account.model.entity.CostAccountTask;
import com.hscloud.hs.cost.account.model.entity.CostAccountTaskNew;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "核算任务Vo")
public class CostAccountTaskVo extends CostAccountTaskNew {

    @Schema(description = "创建人姓名")
    private String name;

    @Schema(description = "创建人工号")
    private String jobNumber;


}
