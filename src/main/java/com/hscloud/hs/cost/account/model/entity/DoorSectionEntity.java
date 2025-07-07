package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 门诊收入
 * 
 * @author zjd
 * @email zjd@gmail.com
 * @date 2023-09-07 15:01:08
 */
@Data
@Schema(description = "门诊收入")
@TableName("cost_door_section")
public class DoorSectionEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 时间id
	 */
	@TableId(value = "dt", type = IdType.ASSIGN_ID)
	@Schema(description = "时间id")
	private String dt;
	/**
	 * 收入类型编码
	 */
	@Schema(description = "收入类型编码")
	private String incomeTypeCode;
	/**
	 * 费用类型/门诊收入,住院收入，医技收入等
	 */
	@Schema(description = "费用类型/门诊收入,住院收入，医技收入等")
	private String incomeTypeName;
	/**
	 * 院区编号
	 */
	@Schema(description = "院区编号")
	private String branchCode;
	/**
	 * 院区名称
	 */
	@Schema(description = "院区名称")
	private String branchName;
	/**
	 * 科室编号
	 */
	@Schema(description = "科室编号")
	private String deptCode;
	/**
	 * 科室名称
	 */
	@Schema(description = "科室名称")
	private String deptName;
	/**
	 * 医生编号
	 */
	@Schema(description = "医生编号")
	private String empCode;
	/**
	 * 医生名称
	 */
	@Schema(description = "医生名称")
	private String empName;
	/**
	 * 科别-挂号科室编码
	 */
	@Schema(description = "科别-挂号科室编码")
	private String regDeptCode;
	/**
	 * 科别-挂号科室编码
	 */
	@Schema(description = "科别-挂号科室编码")
	private String regDeptName;
	/**
	 * 大类编号
	 */
	@Schema(description = "大类编号")
	private String categoryId;
	/**
	 * 大类名称
	 */
	@Schema(description = "大类名称")
	private String categoryName;
	/**
	 * 小类编号
	 */
	@Schema(description = "小类编号")
	private String itemCode;
	/**
	 * 小类名称
	 */
	@Schema(description = "小类名称")
	private String itemName;
	/**
	 * 金额
	 */
	@Schema(description = "金额")
	private BigDecimal fee;
	/**
	 * 数量
	 */
	@Schema(description = "数量")
	private Integer cnt;
	/**
	 * 计算时间
	 */
	@Schema(description = "计算时间")
	private Date createTime;
	/**
    * 每页显示条数，默认 10
    */
	private long pageSize = 10;

	/**
     * 起始索引
     */
	private long startIndex=0;
	/**
     * 所属租户id
     */
	@Schema(description = "所属租户id")
	private Long tenantId;

	@Schema(description = "是否第三方同步")
	private Boolean isSync;
}
