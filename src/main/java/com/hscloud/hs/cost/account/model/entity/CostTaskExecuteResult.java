package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 任务执行结果(CostTaskExecuteResult)表实体类
 *
 * @author makejava
 * @since 2023-09-21 16:36:52
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TenantTable
@Schema(description = "核算任务计算结果表")
public class CostTaskExecuteResult extends Model<CostTaskExecuteResult> {
    //主键ID
    private Long id;
    //任务id
    private Long taskId;
    //核算单元id
    private Long unitId;
    //核算单元名称
    private String unitName;
    //核算分组id
    private String groupId;
    //核算分组名
    private String groupName;
    //总核算值
    private BigDecimal totalCount;
    //计算公式的描述
    private String calculateFormulaDesc;
    //计算明细
    private Object calculateDetail;
    //租户id
    private Long tenantId;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public BigDecimal getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(BigDecimal totalCount) {
        this.totalCount = totalCount;
    }

    public String getCalculateFormulaDesc() {
        return calculateFormulaDesc;
    }

    public void setCalculateFormulaDesc(String calculateFormulaDesc) {
        this.calculateFormulaDesc = calculateFormulaDesc;
    }

    public Object getCalculateDetail() {
        return calculateDetail;
    }

    public void setCalculateDetail(Object calculateDetail) {
        this.calculateDetail = calculateDetail;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
    }

