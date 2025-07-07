package com.hscloud.hs.cost.account.model.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.pig4cloud.pigx.common.excel.annotation.ExcelLine;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 科室单元excel 对应的实体
 * @author Admin
 */
@Data
@ColumnWidth(30)
public class CostAccountUnitExcelVO implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 导入时候回显行号
	 */
	@ExcelLine
	@ExcelIgnore
	private Long lineNum;

	/**
	 * 科室单元
	 */
	@ExcelProperty("科室单元")
	@NotBlank(message = "科室单元不能为空")
	private String unitName;



	@ExcelProperty("科室名称")
	@NotBlank(message = "科室名称不能为空")
	private String deptName;


	@ExcelProperty("人员姓名")
	@NotBlank(message = "人员姓名不能为空")
	private String userName;


	/**
	 * 手机号
	 */
    @NotBlank(message = "核算组别不能为空")
	@ExcelProperty("核算组别")
	private String groupName;


}
