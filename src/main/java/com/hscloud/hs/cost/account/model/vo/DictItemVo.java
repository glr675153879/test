package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 字典项信息
 * @author banana
 * @create 2023-09-11 16:11
 */
@Data
@Schema(description = "字典项信息")
public class DictItemVo {

    @Schema(description = "标签名")
    private String label;

    @Schema(description = "数据值")
    private String value;
}
