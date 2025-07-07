package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 小小w
 * @date 2023/12/7 14:28
 */
@Data
@Schema(description = "其他分组业绩绩效列表展示出参")
public class OtherAccountingUnitPerformanceVo {

    @Schema(description = "核算单元id")
    private Long accountUnitId;

    @Schema(description = "核算单元名称")
    private String accountUnitName;

    @Schema(description = "中药房-中药代煎收入")
    private BigDecimal zyfZydjsr=BigDecimal.ZERO;

    @Schema(description = "中药房-制膏量")
    private BigDecimal zyfZgl=BigDecimal.ZERO;

    @Schema(description = "中药房-中药味数")
    private BigDecimal zyfZyws=BigDecimal.ZERO;

    @Schema(description = "中药房-中药贴数")
    private BigDecimal zyfZyts=BigDecimal.ZERO;

    @Schema(description = "临床药学-临床药师会诊")
    private BigDecimal lcyxLcyshz=BigDecimal.ZERO;

    @Schema(description = "临床药学-临床药师查房")
    private BigDecimal lcyxLcyscf=BigDecimal.ZERO;

    @Schema(description = "临床药学-住院处方打回")
    private BigDecimal lcyxZycfdh=BigDecimal.ZERO;

    @Schema(description = "临床药学-用药监护")
    private BigDecimal lcyxYyjh=BigDecimal.ZERO;

    @Schema(description = "临床药学-事前审方")
    private BigDecimal lcyxSqsf=BigDecimal.ZERO;

    @Schema(description = "临床药学-点评门诊处方")
    private BigDecimal lcyxDpmzcf=BigDecimal.ZERO;

    @Schema(description = "临床药学-人工设置处方规则")
    private BigDecimal lcyxRgszcfgz=BigDecimal.ZERO;

    @Schema(description = "临床药学-住院病历")
    private BigDecimal lcyxZybl=BigDecimal.ZERO;

    @Schema(description = "临方炮制室-协定方加工")
    private BigDecimal lfpzsXdfjg=BigDecimal.ZERO;

    @Schema(description = "临方炮制室-临时加工")
    private BigDecimal lfpzsLsjg=BigDecimal.ZERO;

    @Schema(description = "伦理秘书-固定绩效")
    private BigDecimal llmsGdjx=BigDecimal.ZERO;

    @Schema(description = "制剂室-固定绩效")
    private BigDecimal zjsGdjx=BigDecimal.ZERO;

    @Schema(description = "营养科-服务人次")
    private BigDecimal yykFwrc=BigDecimal.ZERO;

    @Schema(description = "西药房-住院床日")
    private BigDecimal xyfZycr=BigDecimal.ZERO;

    @Schema(description = "西药房-门诊西药处方量")
    private BigDecimal xyfMzxycfl=BigDecimal.ZERO;

    @Schema(description = "静配中心-西药平均绩效80%")
    private BigDecimal xyfXypjjx=BigDecimal.ZERO;

    @Schema(description = "营养科-基础绩效")
    private BigDecimal yykJcjx=BigDecimal.ZERO;

    @Schema(description = "鄞州门诊-门诊非药物治疗开单")
    private BigDecimal yzmzMzfywzlkd=BigDecimal.ZERO;

    @Schema(description = "鄞州门诊-门诊非药物治疗执行")
    private BigDecimal yzmzMzfywzlzx=BigDecimal.ZERO;

    @Schema(description = "鄞州门诊-临方加工")
    private BigDecimal yzmzLfjg=BigDecimal.ZERO;

    @Schema(description = "鄞州门诊-门诊诊察")
    private BigDecimal yzmzMzzc=BigDecimal.ZERO;

    @Schema(description = "鄞州门诊-门诊中药贴数")
    private BigDecimal yzmzMzzyts=BigDecimal.ZERO;

    @Schema(description = "鄞州门诊-退休业绩分")
    private BigDecimal yzmzTxyjf=BigDecimal.ZERO;

    @Schema(description = "鄞州门诊-业绩分")
    private BigDecimal yzmzYjf=BigDecimal.ZERO;

    @Schema(description = "鄞州门诊-膏方")
    private BigDecimal yzmzGf=BigDecimal.ZERO;

    @Schema(description = "考核得分")
    private BigDecimal khdf=BigDecimal.ZERO;

    @Schema(description = "医院奖罚")
    private BigDecimal yyjf=BigDecimal.ZERO;

    @Schema(description = "总核算值")
    private BigDecimal total=BigDecimal.ZERO;

    @Schema(description = "总成本绩效")
    private BigDecimal costTotal=BigDecimal.ZERO;

    @Schema(description = "总收入绩效")
    private BigDecimal incomeTotal=BigDecimal.ZERO;
}
