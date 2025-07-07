package com.hscloud.hs.cost.account.service.impl.second.async;

import cn.hutool.json.JSONObject;
import com.hscloud.hs.cost.account.constant.CacheConstants;
import com.hscloud.hs.cost.account.model.entity.second.Attendance;
import com.hscloud.hs.cost.account.model.entity.second.ProgDetailItem;
import com.hscloud.hs.cost.account.model.entity.second.ProgProject;
import com.hscloud.hs.cost.account.model.entity.second.ProgProjectDetail;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiReportCodeDTO;
import com.hscloud.hs.cost.account.service.kpi.IKpiReportService;
import com.hscloud.hs.cost.account.service.second.IAttendanceService;
import com.hscloud.hs.cost.account.service.second.IProgDetailItemService;
import com.hscloud.hs.cost.account.service.second.IProgProjectDetailService;
import com.hscloud.hs.cost.account.service.second.IProgProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
* 缓存 服务实现类
*
*/
@Service
@RequiredArgsConstructor
public class SecRedisService {
    @Lazy
    @Resource
    private IProgProjectService progProjectService;
    @Lazy
    @Resource
    private IProgProjectDetailService progProjectDetailService;
    @Lazy
    @Resource
    private IProgDetailItemService progDetailItemService;
    @Lazy
    @Resource
    private IAttendanceService attendanceService;
    @Lazy
    @Resource
    private  IKpiReportService kpiReportService;

    @Cacheable(value = CacheConstants.SEC_START_PROJECT,key = "#cycle" ,unless = "#result==null")
    public List<ProgProject> projectList(String cycle) {
        return progProjectService.list();
    }

    @Cacheable(value = CacheConstants.SEC_START_DETAIL,key = "#cycle" ,unless = "#result==null")
    public List<ProgProjectDetail> detailList(String cycle) {
        return progProjectDetailService.list();
    }

    @Cacheable(value = CacheConstants.SEC_START_ITEM,key = "#cycle" ,unless = "#result==null")
    public List<ProgDetailItem> itemList(String cycle) {
        return progDetailItemService.list();
    }

    @Cacheable(value = CacheConstants.SEC_START_ATTENDANCE,key = "#cycle" ,unless = "#result==null")
    public List<Attendance> attendanceList(String cycle) {
        return attendanceService.listByCycle(cycle);
    }

    @CacheEvict(value = {
            CacheConstants.SEC_START_PROJECT,
            CacheConstants.SEC_START_DETAIL,
            CacheConstants.SEC_START_ITEM,
            CacheConstants.SEC_START_ATTENDANCE,
            CacheConstants.SEC_START_ACCOUNTITEMVALUE},
            allEntries = true)
    public void clearCache(String cycle) {
        System.out.println("已清除缓存: " + cycle);
    }


    @Cacheable(value = CacheConstants.SEC_START_ACCOUNTITEMVALUE,key = "#cycle+'_'+#itemType+'_'+#itemCode" ,unless = "#result==null")
    public List<JSONObject> accountItemValueCache(String cycle, String itemType, String itemCode) {
        KpiReportCodeDTO dto = new KpiReportCodeDTO();
        dto.setCycle(Long.parseLong(cycle));
        if (Objects.equals(itemType, "1")){
            dto.setItemCodes(itemCode);
        }else  if (Objects.equals(itemType, "2")){
            dto.setIndexCodes(itemCode);
        }
        return kpiReportService.report(dto).getList();
    }

}
