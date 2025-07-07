package com.hscloud.hs.cost.account.model.vo.second.export;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.pig4cloud.pigx.common.excel.annotation.ExcelLine;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 *  导入错误日志
 */
@Data
public class ImportErrLogVo implements Serializable {

	/**
	 * 导入时候回显行号
	 */
	@ExcelLine
	@ExcelIgnore
	private Long lineNum;

	@ExcelProperty("错误说明")
	private String content;


}
