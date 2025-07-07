package com.hscloud.hs.cost.account.model.vo.report;

import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author : [pc]
 * @version : [v1.0]
 * @createTime : [2024/4/29 11:30]
 */
@Data
@Schema(description = "入参")

public class ReportConditionVo {

    @Column(name = "入参编码")
    String paramCode;

    @Column(name = "入参名称")
    String paramName;

}
