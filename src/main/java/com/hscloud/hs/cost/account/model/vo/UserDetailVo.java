package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户信息Vo")
public class UserDetailVo {

    @Schema(description = "创建人id")
    private Long id;

    @Schema(description = "创建人姓名")
    private String name;

}
