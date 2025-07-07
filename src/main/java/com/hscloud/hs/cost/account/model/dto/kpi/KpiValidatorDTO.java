package com.hscloud.hs.cost.account.model.dto.kpi;

import com.alibaba.fastjson.JSON;
import com.hscloud.hs.cost.account.constant.enums.FieldEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.ItemResultEnum;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 */
@Data
@Schema(description = "核算项校验DTO")
public class KpiValidatorDTO {
    @Schema(description = "口径颗粒度 1人2科室3归集4固定值5多条件")
    @NotBlank(message = "请输入口径颗粒度")
    private String caliber;

    @Schema(description = "周期")
    private String period;

    @Schema(description = "sql")
    private String sql;

    @Schema(description = "sql条件")
    private String whereSql;

    @Schema(description = "核算项id")
    private Long itemId;

    @Schema(description = "查询条件列表")
    private List<KpiItemCondDto> condList;

    @Schema(description = "上报项id")
    private Long reportId;

    @Schema(description = "参数")
    private List<KpiValidatorDTO.SqlValidatorParam> params;

    @Schema(description = "指标保留小数")
    @NotNull(message = "请输入指标保留小数")
    private Integer retainDecimal;

    @Schema(description = "进位规则 1四舍五入 2向上取整 3向下取整")
    @NotBlank(message = "请输入进位规则")
    private String carryRule;

    @Data
    @Schema(description = "sql配置校验参数")
    public static class SqlValidatorParam {
        @Schema(description = "参数逻辑 与-and, 非-not，默认与-and")
        private String relation = "等于";

        @Schema(description = "参数类型 date string")
        private String type;

        @Schema(description = "参数key")
        private String key;

        @Schema(description = "参数值")
        private String value;
    }

    public static KpiValidatorDTO changeToValidatorDTO(KpiItem item, String period) {
        KpiValidatorDTO dto = new KpiValidatorDTO();
        dto.setCaliber(item.getCaliber());
        dto.setPeriod(period.replaceAll("-", ""));
        dto.setSql(item.getConfig());
        dto.setWhereSql(item.getWhereSql());
        dto.setCondList(JSON.parseArray(item.getItemCond(), KpiItemCondDto.class));
        dto.setReportId(item.getReportId());
        dto.setRetainDecimal(item.getRetainDecimal());
        dto.setCarryRule(item.getCarryRule());
        List<KpiValidatorDTO.SqlValidatorParam> params = new ArrayList<>(8);
        KpiValidatorDTO.SqlValidatorParam parameter = new KpiValidatorDTO.SqlValidatorParam();
        parameter.setKey(ItemResultEnum.PERIOD.getType());
        parameter.setType(FieldEnum.DATE.getCode());
        parameter.setValue(period);
        params.add(parameter);
        dto.setParams(params);
        return dto;
    }
}
