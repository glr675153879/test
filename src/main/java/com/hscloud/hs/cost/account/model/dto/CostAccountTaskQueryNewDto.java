package com.hscloud.hs.cost.account.model.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author 小小w
 * @date 2023/11/29 17:07
 */
@Data
@Schema(description = "核算任务查询对象")
@EqualsAndHashCode(callSuper = true)
public class CostAccountTaskQueryNewDto extends PageDto {

    @Schema(description = "核算任务名称")
    private String accountTaskName;

    @Schema(description = "创建人")
    private String name;

    @Schema(description = "核算时间")
    private String accountTime;

    @Schema(description = "核算类型")
    private String accountType;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private String createTime;

    /**
     * 状态
     */
    @Schema(description = "状态 CALCULATING：计算中 EXCEPTION：异常 COMPLETED：已完成 TO_BE_SUBMITTED：待提交")
    private String status;
}
