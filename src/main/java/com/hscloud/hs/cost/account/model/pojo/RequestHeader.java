package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author banana
 * @create 2023-09-19 16:53
 */
@Data
@Schema(description = "请求头信息")
public class RequestHeader {

    @Schema(description = "应用key")
    private String appKey;

    @Schema(description = "时间戳")
    private String timestamp;

    @Schema(description = "签名")
    private String sign;

    @Schema(description = "API名称")
    private String method;

    //将请求参数封装成一个map
    public Map<String, String> getParamsMap() {
        Map<String, String> ret = new HashMap<>();
        ret.put("appkey", appKey);
        ret.put("timestamp", timestamp);
        ret.put("method", method);
        ret.put("sign", sign);
        ret.put("Content-Type", "application/json");
        return ret;
    }
}
