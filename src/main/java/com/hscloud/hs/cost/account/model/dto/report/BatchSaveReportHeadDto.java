package com.hscloud.hs.cost.account.model.dto.report;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import com.hscloud.hs.cost.account.model.entity.report.ReportHead;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * 报表表头批量保存
 *
 * @author zyj
 * @date 2024/04/28
 */
@Data
@Schema(description = "参数")
public class BatchSaveReportHeadDto {

    @NotNull
    @Schema(description = "reportId")
    private Long reportId;

    @Schema(description = "表头集合")
    private List<ReportHead> heads;

}
