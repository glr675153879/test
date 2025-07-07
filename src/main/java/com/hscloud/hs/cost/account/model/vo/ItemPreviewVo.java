package com.hscloud.hs.cost.account.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ItemPreviewVo {

    @Schema(description = "核算项id")
    private Long id;

    @Schema(description = "核算项名称")
    private String accountItemName;


    @Schema(description = "计量单位")
    private String measureUnit;

    @Schema(description = "保留小数位数")
    private Integer retainDecimal;

    @Schema(description = "进位规则")
    private String carryRule;

}
