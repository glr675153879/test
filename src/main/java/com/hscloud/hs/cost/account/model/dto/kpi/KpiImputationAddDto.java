package com.hscloud.hs.cost.account.model.dto.kpi;

import com.baomidou.mybatisplus.annotation.TableField;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.hscloud.hs.cost.account.model.dto.PageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Classname ImputationSearchDto
 * @Description TODO
 * @Date 2024-09-14 15:07
 * @Created by sch
 */
@Data
public class KpiImputationAddDto {

    private Long id;

    @Schema(description = "归集分组code")
    private String categoryCode;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "优先级")
    private Long seq;

    @Schema(description = "规则类型 1特殊归集2无需归集 3归集科室 群组专用")
    private String ruleType;

    @Schema(description = "归集原因")
    private String reason;

    @Schema(description = "人员 1个人 2人员分组 3核算项 4科室id")
    private String people;

    @Schema(description = "科室单元id")
    private Long deptId;

    @Schema(description="逗号切割存一份 member存一份")
    private String memberIds;

    private String busiType;
}
