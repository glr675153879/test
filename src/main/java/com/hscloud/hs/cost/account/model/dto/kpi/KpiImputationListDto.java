package com.hscloud.hs.cost.account.model.dto.kpi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsAutoIncrement;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Classname KpiImputationListDto
 * @Description TODO
 * @Date 2024-09-18 10:49
 * @Created by sch
 */
@Data
public class KpiImputationListDto {


    @Schema()
    private Long id;

    @Schema(description="归集分组code")
    private String categoryCode;


    @Schema(description="规则名称")
    private String ruleName;


    @Schema(description="优先级")
    private Long seq;


    @Schema(description="规则类型 1特殊归集2无需归集")
    private String ruleType;

    @Schema(description="规则类型名称 1特殊归集2无需归集")
    private String ruleTypeName;


    @Schema(description="归集原因")
    private String reason;


    @Schema(description="人员 1个人 2人员分组")
    private String people;

    @Schema(description="人员 1个人 2人员分组")
    private String people_name;


    @Schema(description="科室单元id")
    private Long deptId;

    @Schema(description="科室单元名称")
    private String deptName;


    @Schema(description="逗号切割存一份 member存一份")
    private String memberIds;

    @Schema(description="逗号切割存一份 member存一份")
    private String memberNames;
    @Schema(description="逗号切割存一份 member存一份")
    private String originNames;
}
