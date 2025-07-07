package com.hscloud.hs.cost.account.service.impl.imputation;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.DeptOrUserConstant;
import com.hscloud.hs.cost.account.mapper.imputation.PersonChangeMapper;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationDetails;
import com.hscloud.hs.cost.account.model.entity.imputation.PersonChange;
import com.hscloud.hs.cost.account.model.vo.imputation.ImputationDetailsVO;
import com.hscloud.hs.cost.account.service.imputation.IPersonChangeService;
import com.hscloud.hs.cost.account.utils.CommonUtils;
import com.pig4cloud.pigx.common.data.monitor.enums.DataOpEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author tianbo
 * @Description：
 * @date 2024/8/6 14:47
 */
@Service
public class PersonChangeService extends ServiceImpl<PersonChangeMapper, PersonChange> implements IPersonChangeService {

    /**
     * 记录归集人员变更动作
     *
     * @param imputationDetails
     * @param imputationDetailsVOs
     * @param imputationCycle
     * @param accountUnitId
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void savePersonChange(List<ImputationDetailsVO> imputationDetails, List<ImputationDetailsVO> imputationDetailsVOs, String imputationCycle, Long accountUnitId) {
        if (CollectionUtils.isEmpty(imputationDetails) || CollectionUtils.isEmpty(imputationDetailsVOs)) {
            return;
        }

        List<PersonChange> personChanges = new ArrayList<>();

        Map<Long, ImputationDetailsVO> newId2DetailsMap = imputationDetails.stream().collect(Collectors.toMap(ImputationDetails::getImputationIndexId, Function.identity(), (v1, v2) -> v1));
        Map<Long, ImputationDetailsVO> oldId2DetailsMap = imputationDetailsVOs.stream().collect(Collectors.toMap(ImputationDetails::getImputationIndexId, Function.identity(), (v1, v2) -> v1));

        newId2DetailsMap.forEach((k, v) -> {
            ImputationDetailsVO oldImputationDetailsVO = oldId2DetailsMap.get(k);
            Map<String, Object> oldUser = oldImputationDetailsVO.getLeaderUser();
            Map<String, Object> newUser = v.getLeaderUser();

            String oldUserIds = CommonUtils.getValueFromUserObj(oldUser, "id", DeptOrUserConstant.USER_LIST);
            String oldUserNames = CommonUtils.getValueFromUserObj(oldUser, "name", DeptOrUserConstant.USER_LIST);

            String newUserIds = CommonUtils.getValueFromUserObj(newUser, "id", DeptOrUserConstant.USER_LIST);
            String newUserNames = CommonUtils.getValueFromUserObj(newUser, "name", DeptOrUserConstant.USER_LIST);


            // 将逗号分隔的字符串转换为集合
            Map<String, String> oldUserMap = convertToMap(oldUserIds, oldUserNames);
            Map<String, String> newUserMap = convertToMap(newUserIds, newUserNames);

            // 找出新增的用户
            Map<String, String> addedUsers = newUserMap.entrySet().stream()
                    .filter(entry -> !oldUserMap.containsKey(entry.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            // 找出删除的用户
            Map<String, String> removedUsers = oldUserMap.entrySet().stream()
                    .filter(entry -> !newUserMap.containsKey(entry.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


            fillPersonChanges(personChanges, addedUsers, DataOpEnum.CREATE.getCode(), imputationCycle, v.getImputationIndexName(), accountUnitId);
            fillPersonChanges(personChanges, removedUsers, DataOpEnum.DELETE.getCode(), imputationCycle, v.getImputationIndexName(), accountUnitId);

        });

        if (CollectionUtils.isNotEmpty(personChanges)) {
            this.saveBatch(personChanges);
        }

    }

    /**
     * 根据周期查询
     *
     * @param cycle
     * @return
     */
    @Override
    public List<PersonChange> listByCycle(String cycle) {
        return list(Wrappers.<PersonChange>lambdaQuery().eq(PersonChange::getImputationCycle, cycle));
    }

    @Override
    public Map<String, String> convertToMap(String ids, String names) {
        Map<String, String> map = new HashMap<>();
        if (StrUtil.isBlank(ids) || StrUtil.isBlank(names)) {
            return map;
        }
        String[] idArray = ids.split(",");
        String[] nameArray = names.split(",");
        for (int i = 0; i < idArray.length; i++) {
            map.put(idArray[i], nameArray[i]);
        }
        return map;
    }

    private void fillPersonChanges(List<PersonChange> personChanges, Map<String, String> userMap, String type, String imputationCycle, String imputationIndexName, Long accountUnitId) {
        if (MapUtil.isNotEmpty(userMap)) {
            PersonChange personChange = new PersonChange();
            personChange.setImputationCycle(imputationCycle);
            personChange.setOperationType(type);
            personChange.setImputationIndexName(imputationIndexName);
            personChange.setAccountUnitId(accountUnitId);
            personChange.setUserIds(String.join(",", userMap.keySet()));
            personChange.setUserNames(String.join(",", userMap.values()));
            personChanges.add(personChange);

        }
    }



}
