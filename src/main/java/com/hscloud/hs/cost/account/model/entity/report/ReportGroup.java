package com.hscloud.hs.cost.account.model.entity.report;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * 报表分组
 *
 * @author zyj
 * @date 2024/04/28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "报表分组")
@TableName("rp_report_group")
public class ReportGroup extends BaseEntity<ReportGroup> {

    @Column(comment = "分组编码")
    @Schema(description = "分组编码")
    private String groupCode;

    @Column(comment = "分组名称")
    @Schema(description = "分组名称")
    private String groupName;

}
