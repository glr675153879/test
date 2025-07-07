package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务快照表
 * 
 * @author zjd
 * @email zjd@gmail.com
 * @date 2023-09-14 17:28:58
 */
@Data
@Schema(description = "任务快照表")
@EqualsAndHashCode(callSuper = true)
public class CostTaskSnapshot extends Model<CostTaskSnapshot> {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.AUTO)
	@Schema(description = "主键ID")
	private Long id;
	/**
	 * 任务id
	 */
	@Schema(description = "任务id")
	private Long taskId;
	/**
	 * 任务分组id
	 */
	@Schema(description = "任务分组id")
	private Long taskGroupId;
	/**
	 * 快照类型 unit 核算单元 plan 方案
	 */
	@Schema(description = "快照类型 unit 核算单元 plan 方案")
	private String snapshotType;
	/**
	 * 内容
	 */
	@Schema(description = "内容")
	private String context;
	/**
	 * 租户id
	 */
	@Schema(description = "租户id")
	private Long tenantId;

}
