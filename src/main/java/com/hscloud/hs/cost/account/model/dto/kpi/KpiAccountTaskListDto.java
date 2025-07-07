package com.hscloud.hs.cost.account.model.dto.kpi;

import com.baomidou.mybatisplus.annotation.TableField;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.hscloud.hs.cost.account.model.dto.PageDto;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class KpiAccountTaskListDto extends PageDto {

    private String indexName;

    private int createdId;

    private String testFlag;

    //是否下发 Y/N
    private String issuedFlag;
    //1月报2年报
    private String type;

    private Long period;

    private String accountTaskName;
}
