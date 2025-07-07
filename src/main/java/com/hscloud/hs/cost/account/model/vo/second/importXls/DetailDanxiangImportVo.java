package com.hscloud.hs.cost.account.model.vo.second.importXls;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.pig4cloud.pigx.common.excel.annotation.ExcelLine;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 部门导入导出
 */
@Data
public class DetailDanxiangImportVo implements Serializable {

	/**
	 * 导入时候回显行号
	 */
	@ExcelLine
	@ExcelIgnore
	private Long lineNum;


	/**
	 * 部门名称
	 */
	@NotBlank(message = "部门名称不能为空")
	@ExcelProperty("姓名")
	private String name;


	/**
	 * 部门编码
	 */
	@ExcelProperty("工号")
	private String code;

	/**
	 * 排序
	 */
	@ExcelProperty(value = "排序值")
	private Integer sortOrder;

}
