package com.hscloud.hs.cost.account.model.pojo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author banana
 * @create 2023-09-19 16:57
 */
@Data
@Schema(description = "响应信息")
public class ResponseData<T> {
    @Schema(description = "响应码 0:成功")
    private int code;

    @Schema(description = "返回信息")
    private String msg;

    @Schema(description = "数据")
    private T data;

    //判断是否响应成功
    public boolean getSuccess() {
        return code == 0 ? true : false;
    }
}
