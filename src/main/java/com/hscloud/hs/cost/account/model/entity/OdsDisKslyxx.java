package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 消毒系统接口返回数据表
 * @author banana
 * @create 2023-10-16 10:02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "消毒系统接口返回数据表")
public class OdsDisKslyxx extends Model<OdsDisKslyxx> {

    //入参的时候（按日的粒度存）
    //格式：YYYYMMDD
    @Schema(description = "主键id")
    private String dt;

    @Schema(description = "主键id")
    private String seq;

    @Schema(description = "科室编号")
    private String deptCode;

    @Schema(description = "科室名称")
    private String deptName;

    @Schema(description = "一次性物品数量")
    private BigDecimal disposableNum;

    @Schema(description = "非一次性物品数量")
    private BigDecimal nonDisposableNum;

    @Schema(description = "一次性物品金额")
    private BigDecimal disposableAmount;

    @Schema(description = "非一次性物品金额")
    private BigDecimal nonDisposableAmount;

    @Schema(description = "总数量")
    private BigDecimal num;

    @Schema(description = "总金额")
    private BigDecimal amount;

    //创建时间：插入到数据库中的时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createDate;
}
