package com.hscloud.hs.cost.account.model.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.hscloud.hs.cost.account.constant.enums.SignEncryptType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author banana
 * @create 2023-09-19 16:34
 */
@Schema(description = "数据采集接口入参")
@Data
public class GatewayApiDto {

    @Schema(description = "请求url")
    private String url;

    @Schema(description = "引用code")
    private String appCode;

    @Schema(description = "应用key")
    private String appKey;

    @Schema(description = "密钥")
    private String appSecret;

    @Schema(description = "加密类型")
    private SignEncryptType signEncryptType;

    @Schema(description = "API名称")
    private String headerMethodType;

    @TableField(exist = false)
    @Schema(description = "时间戳")
    private String timeStamp;

    @TableField(exist = false)
    @Schema(description = "签名")
    private String sign;

    @TableField(exist = false)
    @Schema(description = "app")
    private String method;

}
