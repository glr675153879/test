package com.hscloud.hs.cost.account.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.DistributionUserInfoMapper;
import com.hscloud.hs.cost.account.model.dto.DistributionUserInfoQueryDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountUnit;
import com.hscloud.hs.cost.account.model.entity.CostUnitRelateInfo;
import com.hscloud.hs.cost.account.model.entity.DistributionUserInfo;
import com.hscloud.hs.cost.account.service.IDistributionUserInfoService;
import com.hscloud.hs.cost.account.utils.SqlUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 人员信息 服务实现类
 * </p>
 *
 * @author author
 * @since 2023-11-20
 */
@Service
@RequiredArgsConstructor
public class DistributionUserInfoServiceImpl extends ServiceImpl<DistributionUserInfoMapper, DistributionUserInfo> implements IDistributionUserInfoService {

    private final DistributionUserInfoMapper userInfoMapper;
    private final SqlUtil sqlUtil;

    /**
     * 分页模糊匹配人员信息
     *
     * @param dto
     * @return
     */
    @Override
    public IPage<DistributionUserInfo> listUserInfo(DistributionUserInfoQueryDto dto) {

        Page page = new Page<>(dto.getCurrent(), dto.getSize());
       return userInfoMapper.listByQueryDto(page, dto);
//        Map<Long, String> userMap = new HashMap<>();
//        //先查询所有科室单元下所有人,插入人员信息表
//        List<CostAccountUnit> unitList = new CostAccountUnit().selectAll();
//        for (CostAccountUnit costAccountUnit : unitList) {
//            //查询科室单元下所有科室
//            final List<CostUnitRelateInfo> deptList = new CostUnitRelateInfo().selectList(Wrappers.<CostUnitRelateInfo>lambdaQuery()
//                    .eq(CostUnitRelateInfo::getAccountUnitId, costAccountUnit.getId())
//                    .eq(CostUnitRelateInfo::getType, "dept"));
//            //查询所有科室下所有人
//            if (deptList.size() > 0) {
//                //获取科室下所有的人员
//                List<Long> deptIds = deptList.stream().map(CostUnitRelateInfo::getRelateId).map(Long::parseLong).collect(Collectors.toList());
//                userMap = sqlUtil.getUserIdsAndNamesByDeptIds(deptIds);
//            }
//            //获取该科室单元下所有的人员
//            List<CostUnitRelateInfo> userList = new CostUnitRelateInfo().selectList(Wrappers.<CostUnitRelateInfo>lambdaQuery()
//                    .eq(CostUnitRelateInfo::getAccountUnitId, costAccountUnit.getId())
//                    .eq(CostUnitRelateInfo::getType, "user"));
//
//            Map<Long, String> idNames = userList.stream().collect(Collectors.toMap(info -> Long.parseLong(info.getRelateId()), CostUnitRelateInfo::getName));
//            //将所有关联科室单元的人添加到部门获取的map
//            userMap.putAll(idNames);
//            //查询所有已经存在的人员信息
//            List<DistributionUserInfo> userInfoList = new DistributionUserInfo().selectAll();
//            //单独获取人员id
//            if (userInfoList.isEmpty()) {
//                List<Long> userIdList = userInfoList.stream().map(DistributionUserInfo::getUserId).collect(Collectors.toList());
//                // 使用Stream对userMap进行过滤，剔除掉已经保存的人员
//                Map<Long, String> filteredUserMap = userMap.entrySet().stream()
//                        .filter(entry -> !userIdList.contains(entry.getKey()))
//                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//                //遍历map,封装为UserInfo对象落库
//                List<DistributionUserInfo> distributionUserInfoList = filteredUserMap.entrySet().stream()
//                        .map(entry -> {
//                            DistributionUserInfo distributionUserInfo = new DistributionUserInfo();
//                            distributionUserInfo.setUserId(entry.getKey());
//                            distributionUserInfo.setUserName(entry.getValue());
//                            distributionUserInfo.setUnitId(costAccountUnit.getId());
//                            distributionUserInfo.setUnitName(costAccountUnit.getName());
//                            distributionUserInfo.setAccountGroupCode(costAccountUnit.getAccountGroupCode());
//                            return distributionUserInfo;
//                        }).collect(Collectors.toList());
//                //插入数据库
//                saveBatch(distributionUserInfoList);
//            }
//        }
//        //模糊匹配人员信息表,返回对象
//       // Page page = new Page<>(dto.getCurrent(), dto.getSize());
//        return userInfoMapper.listByQueryDto(page, dto);
    }
}
