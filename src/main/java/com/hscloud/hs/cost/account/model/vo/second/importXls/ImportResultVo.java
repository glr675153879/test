package com.hscloud.hs.cost.account.model.vo.second.importXls;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.pig4cloud.pigx.common.excel.annotation.ExcelLine;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 *  导入结果
 */
@Data
public class ImportResultVo implements Serializable {

	@Schema(description = "总共导入条数")
	private int totalCount;

	@Schema(description = "错误数据条数")
	private int errorCount;

}
