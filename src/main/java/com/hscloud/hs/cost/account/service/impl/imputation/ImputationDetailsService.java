package com.hscloud.hs.cost.account.service.impl.imputation;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hscloud.hs.cost.account.model.entity.imputation.Imputation;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationDeptUnit;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationIndex;
import com.hscloud.hs.cost.account.model.entity.second.Attendance;
import com.hscloud.hs.cost.account.service.imputation.IImputationDeptUnitService;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.imputation.ImputationDetailsMapper;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationDetails;
import com.hscloud.hs.cost.account.service.imputation.IImputationDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 归集明细 服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImputationDetailsService extends ServiceImpl<ImputationDetailsMapper, ImputationDetails> implements IImputationDetailsService {

    private final ImputationDetailsMapper imputationDetailsMapper;

    @Autowired
    @Lazy
    private IImputationDeptUnitService imputationDeptUnitService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addByListBatch(Imputation imputation, List<ImputationDeptUnit> deptUnitList, List<ImputationIndex> indexList, List<Attendance> attendanceList) {
        Long imputationId = imputation.getId();

        List<ImputationDetails> dbList = this.listByPId(imputationId);
        //key : deptUnitId+"-"+indexId
        Map<String, ImputationDetails> dbMap = dbList.stream().collect(Collectors.toMap(item -> item.getAccountUnitId() + "_" + item.getImputationIndexId(), Function.identity(), (v1, v2) -> v1));
        List<ImputationDetails> addOrEditList = new ArrayList<>();

        for (ImputationDeptUnit deptUnit : deptUnitList) {
            if (deptUnit.getAccountUnitName().equals("门办")) {
                System.out.println("menban");
            }
            List<Attendance> userList = attendanceList.stream().filter(item -> Objects.equals(deptUnit.getAccountUnitId() + "", item.getAccountUnitId())).collect(Collectors.toList());
            String userIds = userList.stream().map(item -> item.getUserId() + "").collect(Collectors.joining(","));
            String empNames = userList.stream().map(Attendance::getEmpName).collect(Collectors.joining(","));
            for (ImputationIndex index : indexList) {
                ImputationDetails details = this.createDetails(imputation, dbMap, deptUnit, index);
                details.setUserIds(userIds);
                details.setEmpNames(empNames);
                addOrEditList.add(details);
            }
        }
        //更新人员
        imputationDeptUnitService.updatePersons(addOrEditList, imputation.getImputationCycle());
        if (!addOrEditList.isEmpty()) {
            this.saveOrUpdateBatch(addOrEditList);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addByList(Imputation imputation, List<ImputationDeptUnit> deptUnitList, List<ImputationIndex> indexList, List<Attendance> attendanceList) {
        String userIds = attendanceList.stream().map(item -> item.getUserId() + "").collect(Collectors.joining(","));
        String empNames = attendanceList.stream().map(Attendance::getEmpName).collect(Collectors.joining(","));

        Long imputationId = imputation.getId();
        List<ImputationDetails> dbList = this.listByPId(imputationId);
        //key : deptUnitId+"-"+indexId
        Map<String, ImputationDetails> dbMap = dbList.stream().collect(Collectors.toMap(item -> item.getAccountUnitId() + "_" + item.getImputationIndexId(), Function.identity(), (v1, v2) -> v1));
        List<ImputationDetails> addOrEditList = new ArrayList<>();
        for (ImputationDeptUnit deptUnit : deptUnitList) {
            for (ImputationIndex index : indexList) {
                ImputationDetails details = this.createDetails(imputation, dbMap, deptUnit, index);
                details.setUserIds(userIds);
                details.setEmpNames(empNames);
                addOrEditList.add(details);
            }
        }

        //更新人员
        imputationDeptUnitService.updatePersons(addOrEditList, imputation.getImputationCycle());
        if (!addOrEditList.isEmpty()) {
            this.saveOrUpdateBatch(addOrEditList);
        }
    }

    @Override
    public List<ImputationDetails> listByPId(Long imputationId) {
        return this.list(Wrappers.<ImputationDetails>lambdaQuery()
                .eq(ImputationDetails::getImputationId, imputationId));
    }

    @Override
    public ImputationDetails createDetails(Imputation imputation, Map<String, ImputationDetails> dbMap, ImputationDeptUnit deptUnit, ImputationIndex index) {
        Long imputationId = imputation.getId();
        String key = deptUnit.getId() + "_" + index.getId();
        ImputationDetails details = dbMap.get(key);
        if (details == null) {
            details = new ImputationDetails();
            details.setImputationId(imputationId);
            details.setImputationCode(imputation.getImputationCode());
            details.setImputationName(imputation.getImputationName());
            details.setImputationCycle(imputation.getImputationCycle());

            details.setImputationDeptUnitId(deptUnit.getId());
            details.setImputationIndexId(index.getId());
            details.setImputationIndexName(index.getName());

            details.setAccountUnitId(deptUnit.getAccountUnitId());
            details.setAccountUnitName(deptUnit.getAccountUnitName());
        }
        return details;
    }

    @Override
    public void saveOrUpdateBatchImputationDetails(List<ImputationDetails> addOrEditList) {
        imputationDetailsMapper.saveOrUpdateBatchImputationDetails(addOrEditList);
    }

}
