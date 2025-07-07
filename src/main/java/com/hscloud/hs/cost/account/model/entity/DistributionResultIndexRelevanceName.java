package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author author
 * @since 2023-11-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("distribution_result_index_relevance_name")
@Schema(description="核算任务关联表")
public class DistributionResultIndexRelevanceName extends Model<DistributionResultIndexRelevanceName> {

    private static final long serialVersionUID = 1L;

    @Schema(description= "主键id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description= "中文名称")
    private String name;

    @Schema(description= "对应字段")
    private String fields;

    @Schema(description= "对应字段")
    private String taskType;


}
