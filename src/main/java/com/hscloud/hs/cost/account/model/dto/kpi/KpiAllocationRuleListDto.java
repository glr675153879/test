package com.hscloud.hs.cost.account.model.dto.kpi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.hscloud.hs.cost.account.model.dto.PageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
* 分摊公式Model
* @author you
* @since 2024-09-09
*/

@Data
@Schema(description = "分摊公式表")
public class KpiAllocationRuleListDto extends PageDto {
    @Schema(description = "指标code")
    private String indexCode;
}