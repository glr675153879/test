package com.hscloud.hs.cost.account.model.vo.kpi;

import com.baomidou.mybatisplus.annotation.TableField;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Administrator
 */
@Data
@Schema(description = "配置表")
public class KpiConfigVO {
    @Schema(description = "id")
    private Long id;

    @Schema(description = "周期")
    private Long period;

    @Schema(description = "人员锁定标记Y/N")
    private String userFlag;

    @Schema(description = "人员锁定时间")
    private Date attendanceUpdateDate;

    @Schema(description = "指标项重抽标记Y/N")
    private String indexFlag;

    @Schema(description = "指标项最近抽取时间")
    private Date indexUpdateDate;

    @Schema(description = "是否下发 Y/N")
    private String issuedFlag;

    @Schema(description = "下发时间")
    private Date issuedDate;

    @Schema(description = "下发子任务id")
    private Long taskChildId;

    @Schema(description = "归集重算标记Y/N")
    private String imputationFlag;

    @Schema(description = "归集最近重算时间")
    private Date imputationDate;

    @Schema(description = "租户id")
    private Long tenantId;

    @Schema(description = "是否默认周期 Y/N")
    private String defaultFlag;

    @Schema(description = "任务名")
    private String taskName;


    @TableField(value = "user_flag_ks")
    @Column(comment = "人员锁定标记Y/N 科室", type = MySqlTypeConstant.CHAR, length = 1)
    private String userFlagKs;


    @TableField(value = "attendance_update_date_ks")
    @Column(comment = "人员锁定时间-科室成本", type = MySqlTypeConstant.DATETIME)
    private Date attendanceUpdateDateKs;

    @TableField(value = "default_ks_flag")
    @Column(comment = "是否默认周期 Y/N 科室", type = MySqlTypeConstant.CHAR, length = 1)
    private String defaultKsFlag;

    @Schema(description = "当量锁定标记 Y/N")
    private String equivalentFlag;

    @Schema(description = "当量锁定时间")
    private Date equivalentUpdateDate;

    @Schema(description = "当量指标项抽取 0-抽取中，1-抽取完成，9-抽取异常结束")
    private String equivalentIndexFlag;

    @Schema(description = "当量指标项最近抽取时间")
    private Date equivalentIndexUpdateDate;

    @Schema(description = "绩效签发锁定标记 Y/N")
    private String signFlag;

    @Schema(description = "非当量核算项最近抽取时间")
    private Date nonEquivalentIndexUpdateDate;

    @Schema(description = "当量单价")
    private BigDecimal equivalentPrice;

    public static KpiConfigVO changeToVo(KpiConfig config) {
        KpiConfigVO vo = new KpiConfigVO();
        BeanUtils.copyProperties(config, vo);
        return vo;
    }
}
