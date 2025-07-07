package com.hscloud.hs.cost.account.model.entity.second;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.constant.enums.second.ProjectType;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hf
 * @since 2023-10-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "方案核算指标")
@TableName("sec_prog_project")
public class ProgProject extends BaseEntity<ProgProject> {

    private static final long serialVersionUID = 1L;

    @Column(comment = "核算方案")
    @Schema(description = "核算方案id")
    private Long programmeId;

    @Column(comment = "项目名称")
    @Schema(description = "项目名称")
    private String name;

    @Column(comment = "绩效类型 字典")
    @Schema(description = "绩效类型 字典")
    private String projectType;

    @Column(comment = "保留位数")
    @Schema(description = "保留位数")
    private Integer reservedDecimal;

    @Column(comment = "进位规则")
    @Schema(description = "进位规则 字典")
    private String carryRule;

    @Column(comment = "排序号")
    @Schema(description = "排序号")
    private Integer sortNum;

    //冗余字段
    @Column(comment = "模板id")
    @Schema(description = "模板id")
    private Long commonId;
    //冗余字段

    @Column(comment = "common模板方案id")
    @Schema(description = "common模板方案id")
    private Long progCommonId;

    @TableField(exist = false)
    @Schema(description = "操作标记")
    private String actionType;

    @TableField(exist = false)
    List<ProgProjectDetail> progProjectDetailList = new ArrayList<>();


}
