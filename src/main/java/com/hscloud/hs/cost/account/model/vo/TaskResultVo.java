package com.hscloud.hs.cost.account.model.vo;

import com.hscloud.hs.cost.account.model.entity.CostAccountTask;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "核算任务Vo")
public class TaskResultVo extends CostAccountTask {


    @Schema(description = "核算单元名称")
    private String name;

    @Schema(description = "核算分组名称")
    private String AccountGroupCode;



    @Schema(description = "核算分组名称")
    private String groupName;

    @Schema(description = "核算分组类型")
    private String groupType;

    @Schema(description = "核算对象")
    private String accountObject;

}
