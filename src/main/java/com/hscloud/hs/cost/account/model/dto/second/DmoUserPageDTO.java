package com.hscloud.hs.cost.account.model.dto.second;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "数据中台 人员page")
public class DmoUserPageDTO extends Page {

    @Column(comment = "姓名")
    @Schema(description = "姓名")
    private String empName;

    @Column(comment = "工号")
    @Schema(description = "工号")
    private String empCode;

    @Column(comment = "科室")
    @Schema(description = "科室")
    private String deptName;

    @Column(comment = "职务")
    @Schema(description = "职务")
    private String postName;


}
