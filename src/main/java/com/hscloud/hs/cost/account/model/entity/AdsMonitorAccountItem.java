package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author  lian
 * @date  2023-09-19 12:30
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "监测动态监测值数据")
@Entity
@Table(name = "ads_monitor_acount_item")
public class AdsMonitorAccountItem extends Model<AdsMonitorAccountItem> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    @Id
    private Long id;

    @Schema(description = "科室单元id")
    private Long accountUnitId;

    @Schema(description = "核算项id")
    private Long accountItemId;

    @Schema(description = "科室单元名称")
    private String accountUnitName;

    @Schema(description = "核算项名称")
    private String accountItemName;

    @Schema(description = "监测值")
    private Long value;

    @Schema(description = "监测日期")
    private String dt;

}
