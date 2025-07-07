package com.hscloud.hs.cost.account.model.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.hscloud.hs.cost.account.model.pojo.UnitAccountInfo;
import com.hscloud.hs.cost.account.model.pojo.WholeAccountInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Admin
 */
@Data
@Schema(description = "核算任务结果详情Vo")
public class CostAccountTaskResultDetailVo implements java.io.Serializable{

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "核算开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime accountStartTime;

    @Schema(description = "核算结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime accountEndTime;

    @Schema(description = "总的核算信息")
    private WholeAccountInfo wholeAccountInfo;


    @Schema(description = "核算单元核算信息")
    private Page<UnitAccountInfo> unitAccountInfoList;

}



