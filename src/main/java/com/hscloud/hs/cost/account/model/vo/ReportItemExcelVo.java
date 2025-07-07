package com.hscloud.hs.cost.account.model.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.pig4cloud.pigx.common.excel.annotation.ExcelLine;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "上报项Excel")
public class ReportItemExcelVo implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 导入时候回显行号
	 */
	@ExcelLine
	@ExcelIgnore
	private Long lineNum;

	@ExcelProperty("上报项名称")
	private Long id;

	@ExcelProperty("核算项名称")
	private String name;

	@ExcelProperty("上报项单位")
	private String measureUnit;

	@ExcelProperty("上报项说明")
	private String description;

}
