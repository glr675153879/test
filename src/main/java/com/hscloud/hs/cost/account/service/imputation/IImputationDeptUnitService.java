package com.hscloud.hs.cost.account.service.imputation;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.CostUnitRelateInfo;
import com.hscloud.hs.cost.account.model.entity.imputation.Imputation;
import com.hscloud.hs.cost.account.model.dto.imputation.ImputationDeptUnitDTO;
import com.hscloud.hs.cost.account.model.dto.imputation.ImputationDeptUnitDelDTO;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationDeptUnit;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationDetails;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationIndex;
import com.hscloud.hs.cost.account.model.entity.second.Attendance;

import java.util.List;

import com.hscloud.hs.cost.account.model.vo.imputation.CostAccountUnitVO;
import com.hscloud.hs.cost.account.model.vo.imputation.ImputationDeptUnitVO;
import com.hscloud.hs.cost.account.model.vo.imputation.ImputationMatchDeptUnitVO;

/**
 * 归集科室单元 服务接口类
 */
public interface IImputationDeptUnitService extends IService<ImputationDeptUnit> {
    /**
     * 引入上月科室生成数据
     *
     */
    void generateLastMonth(String currentCycle);

    /**
     * 设置人员
     *
     */
    void setPersonByCycle(String currentCycle);

    List<Attendance> getAttendanceList(Imputation imputation, Long accountUnitId, String accountUnitName, String accountGroupCode, ImputationIndex index, String type);

    IPage<CostAccountUnitVO> pageUnmatched(Page page, Long imputationId, String accountUnitName, String accountGroupCode);

    ImputationMatchDeptUnitVO match(ImputationDeptUnitDTO imputationDeptUnitDTO);


    boolean addImputationDeptUnit(ImputationDeptUnitDTO imputationDeptUnitDTO);

    boolean updateImputationDeptUnit(ImputationDeptUnitDTO imputationDeptUnitDTO);

    boolean removeImputationDeptUnit(ImputationDeptUnitDelDTO imputationDeptUnitDelDTO);

    IPage<ImputationDeptUnitVO> pageImputationDeptUnit(Page<ImputationDeptUnit> page, QueryWrapper<ImputationDeptUnit> wrapper, Long imputationId);

    void setIncomePerson(String currentCycle);

    void setWorkPerson(String currentCycle);

    ImputationDeptUnit getGroupByUnitName(String unitName);

    void updatePersons(List<ImputationDetails> imputationDetails, String currentCycle);
}
