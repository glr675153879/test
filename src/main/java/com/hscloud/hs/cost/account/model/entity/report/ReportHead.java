package com.hscloud.hs.cost.account.model.entity.report;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;


/**
 * 报表表头
 *
 * @author zyj
 * @date 2024/04/28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "报表表头")
@TableName("rp_report_head")
public class ReportHead extends BaseEntity<ReportHead> {

    @Column(comment = "reportId")
    @Schema(description = "reportId")
    private Long reportId;

    @Column(comment = "parentId")
    @Schema(description = "parentId")
    private Long parentId;

    @Column(comment = "排序")
    @Schema(description = "排序")
    private Integer sort;

    @Column(comment = "字段id")
    @Schema(description = "字段id")
    private Long fieldId;

    @TableField(exist = false)
    @Schema(description = "子节点list")
    private List<ReportHead> children;

    @TableField(exist = false)
    @Column(comment = "报表字段标题")
    @Schema(description = "报表字段标题")
    private String fieldViewAlias;

}
