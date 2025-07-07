package com.hscloud.hs.cost.account.model.vo.dataReport;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "上报任务变更日志")
public class AssignResultVo {

    @Schema(description = "id")
    private Long taskId;
    @Schema(description = "返回标记：成功标记=0，失败标记=1")
    private int code;
    @Schema(description = "返回信息")
    private String msg;


}

