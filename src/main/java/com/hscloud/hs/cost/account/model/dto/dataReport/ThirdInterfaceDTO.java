package com.hscloud.hs.cost.account.model.dto.dataReport;

import com.alibaba.fastjson.JSONObject;
import com.hscloud.hs.cost.account.model.MethodEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Map;

/**
 * 第三方接口请求DTO
 */
@Data
public class ThirdInterfaceDTO {
	@Schema(description = "请求URL")
	@NotBlank(message = "请求URL不能为空")
	private String url;

	@Schema(description = "请求方式")
	@NotBlank(message = "请求方式不能为空")
	private MethodEnum methodType;

	@Schema(description = "请求头参数")
	private Map<String, String> headers;

	@Schema(description = "请求体")
	private JSONObject body;

}
