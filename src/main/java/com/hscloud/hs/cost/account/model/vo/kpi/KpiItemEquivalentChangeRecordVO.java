package com.hscloud.hs.cost.account.model.vo.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class KpiItemEquivalentChangeRecordVO {
    private Long id;

    @Schema(description = "父级id")
    private Long pEquivalentId;

    @Schema(description = "分组uuid")
    private String groupUuid;

    @Schema(description = "核验任务id")
    private Long taskId;

    @Schema(description = "核算项名称")
    private String itemName;

    @Schema(description = "当量id")
    private Long equivalentId;

    @Schema(description = "调整前")
    private BigDecimal beforeValue;

    @Schema(description = "操作符")
    private String operators;

    @Schema(description = "调整值")
    private BigDecimal changeValue;

    @Schema(description = "调整原因")
    private String reason;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件地址")
    private String fileUrl;

    @Schema(description = "修改人")
    private Long changeUserId;

    @Schema(description = "修改人姓名")
    private String changeUserName;

    @Schema(description = "修改时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdDate;

    @Schema(description = "更改对象")
    private String changeName;

    @Schema(description = "状态 -1:驳回 0:未提交 10:待审核 20:通过")
    private String status;

    @Schema(description = "修改者 0-科室 1-绩效办")
    private String changeFlag;

}
