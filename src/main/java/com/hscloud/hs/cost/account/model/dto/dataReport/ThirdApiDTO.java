package com.hscloud.hs.cost.account.model.dto.dataReport;

import com.pig4cloud.pigx.admin.api.dto.TokenCommonParamDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ThirdApiDTO<T> extends TokenCommonParamDto {

	@Schema(description = "业务数据data")
	private T data;

}
