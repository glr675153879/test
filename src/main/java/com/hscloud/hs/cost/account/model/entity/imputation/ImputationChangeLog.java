package com.hscloud.hs.cost.account.model.entity.imputation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author xiechenyu
 * @Description：
 * @date 2024/4/17 17:51
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "归集变更日志")
@TableName("im_imputation_change_log")
public class ImputationChangeLog extends ImputationBaseEntity<ImputationChangeLog> {
    private static final long serialVersionUID = 1L;

    @Column(comment = "归集主档ID")
    @Schema(description = "归集主档ID")
    private Long imputationId;


    @Column(comment = "应用", length = 10)
    @Schema(description = "应用")
    private String changeModel;

    @Column(comment = "操作类型", length = 10)
    @Schema(description = "操作类型")
    private String changeType;

    @Column(comment = "操作项")
    @Schema(description = "操作项")
    private String changeItem;

    @Column(comment = "操作时间")
    @Schema(description = "操作时间")
    private String changeTime;

    @Column(comment = "操作人")
    @Schema(description = "操作人")
    private String changeUserName;

    @Column(comment = "操作人文字描述")
    @Schema(description = "操作人文字描述")
    private String changeUserNameText;

    @Column(comment = "描述", type= MySqlTypeConstant.TEXT)
    @Schema(description = "描述")
    private String changeDesc;


}
