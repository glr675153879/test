package com.hscloud.hs.cost.account.service.imputation;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.imputation.Imputation;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationDeptUnit;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationDetails;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationIndex;
import com.hscloud.hs.cost.account.model.entity.second.Attendance;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
* 归集明细 服务接口类
*/
public interface IImputationDetailsService extends IService<ImputationDetails> {

    @Transactional(rollbackFor = Exception.class)
    void addByListBatch(Imputation imputation, List<ImputationDeptUnit> deptUnitList, List<ImputationIndex> indexList, List<Attendance> attendanceList);

    /**
     * 添加归集明细
     * @param imputation 归集主档
     * @param deptUnitList 科室单元
     * @param indexList 归集指标
     * @param otherAttendanceList 其他考勤
     */
    void addByList(Imputation imputation, List<ImputationDeptUnit> deptUnitList, List<ImputationIndex> indexList, List<Attendance> otherAttendanceList);

    List<ImputationDetails> listByPId(Long imputationId);

    ImputationDetails createDetails(Imputation imputation, Map<String, ImputationDetails> dbMap, ImputationDeptUnit deptUnit, ImputationIndex index);

    void saveOrUpdateBatchImputationDetails(List<ImputationDetails> addOrEditList);
}
