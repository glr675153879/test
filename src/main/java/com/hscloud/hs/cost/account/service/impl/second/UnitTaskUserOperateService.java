package com.hscloud.hs.cost.account.service.impl.second;

import com.alibaba.fastjson.JSONObject;
import com.hscloud.hs.cost.account.mapper.second.UnitTaskMapper;
import com.hscloud.hs.cost.account.mapper.second.UnitTaskUserMapper;
import com.hscloud.hs.cost.account.model.entity.second.UnitTask;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskUser;
import com.hscloud.hs.cost.account.service.second.IAttendanceService;
import com.hscloud.hs.cost.account.service.second.IGrantUnitService;
import com.hscloud.hs.cost.account.service.second.IUnitTaskService;
import com.hscloud.hs.cost.account.service.second.IUnitTaskUserService;
import com.hscloud.hs.cost.account.utils.CommonUtils;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.common.core.constant.SecurityConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 发放单元任务人员 服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UnitTaskUserOperateService {


    private final UnitTaskMapper unitTaskMapper;
    private final UnitTaskUserMapper unitTaskUserMapper;


    private final IAttendanceService attendanceService;
    private final RemoteUserService remoteUserService;
    @Lazy
    @Autowired
    private IUnitTaskService unitTaskService;
    //@Lazy
    @Autowired
    private IGrantUnitService grantUnitService;
    @Lazy
    @Autowired
    private IUnitTaskUserService unitTaskUserService;


    public List<UnitTaskUser> listByTaskIdCycleUnitId(UnitTask unitTask, List<UnitTaskUser> dmoUserList) {
        Long grantUnitId = unitTask.getGrantUnitId();
        // 根据条件查询数据
        UnitTask lastUnitTask = unitTaskService.getLastUnitTask(unitTask.getId(), grantUnitId);
        if (Objects.nonNull(lastUnitTask)) {
            // 上个月的数据和数据小组给的数据取并集

            // 先全量接收数据小组数据，再去重接收上月用户数据
            Set<String> existingUserCodes = dmoUserList.stream().map(UnitTaskUser::getEmpCode).collect(Collectors.toSet());
            String deptIds = grantUnitService.getDeptIds(grantUnitId);
            List<String> unitIdList = CommonUtils.str2List(deptIds);
            List<UnitTaskUser> userListDB = unitTaskUserService.listByTaskId(lastUnitTask.getId());
            userListDB = userListDB.stream().filter(item -> !existingUserCodes.contains(item.getEmpCode())).collect(Collectors.toList());

            //过滤已经删除的人员
            if (!userListDB.isEmpty()) {
                List<Long> userIdsDB = userListDB.stream().map(o -> Long.parseLong(o.getUserId())).collect(Collectors.toList());
                List<SysUser> usersDB = remoteUserService.getUserListByThird(userIdsDB, SecurityConstants.FROM_IN).getData();
                //List<String> userIds = usersDB.stream().filter(item -> Objects.equals("0", item.getStatus())).map(o -> o.getUserId() + "").collect(Collectors.toList());//如果要过滤禁用人员，换这句
                List<String> userIds = usersDB.stream().map(o -> o.getUserId() + "").collect(Collectors.toList());
                userListDB = userListDB.stream().filter(item -> userIds.contains(item.getUserId())).collect(Collectors.toList());
            }
            log.info("继承上月人员数据 userListDB:"+ JSONObject.toJSONString(userListDB));

            // 获取考勤表中这些人的本月数据
            userListDB = unitTaskUserService.getAttendanceData(unitTask.getCycle(), userListDB, unitIdList);
            dmoUserList.addAll(userListDB);
            return dmoUserList;
        }
        return dmoUserList;
    }

}
