package com.hscloud.hs.cost.account.model.entity.report;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * 报表设计表
 *
 * @author zyj
 * @date 2024/04/28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "报表设计表")
@TableName("rp_report")
public class Report extends BaseEntity<Report> {

    @Column(comment = "编码")
    @Schema(description = "编码")
    private String code;

    @Column(comment = "名称")
    @Schema(description = "名称")
    private String name;

    @Column(comment = "说明")
    @Schema(description = "说明")
    private String note;

    @Column(comment = "应用id")
    @Schema(description = "应用id")
    private Long appId;

    @Column(comment = "分组id")
    @Schema(description = "分组id")
    private Long groupId;

    @TableField(exist = false)
    @Schema(description = "数据集id")
    private Long reportDbId;

}
