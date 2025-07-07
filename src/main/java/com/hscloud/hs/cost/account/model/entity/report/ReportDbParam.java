package com.hscloud.hs.cost.account.model.entity.report;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * 报表入参表
 *
 * @author zyj
 * @date 2024/04/28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "报表入参表")
@TableName("rp_report_param")
public class ReportDbParam extends BaseEntity<ReportDbParam> {

    @Column(comment = "报表id")
    @Schema(description = "报表id")
    private Long reportId;

    @Column(comment = "数据集id")
    @Schema(description = "数据集id")
    private Long reportDbId;

    @Column(comment = "参数编码")
    @Schema(description = "参数编码")
    private String code;

    @Column(comment = "参数变量说明")
    @Schema(description = "参数变量说明")
    private String note;

    @Column(comment = "默认值")
    @Schema(description = "默认值")
    private String defaultValue;

    @Column(comment = "参数编码")
    @Schema(description = "参数编码")
    private String sort;

}
