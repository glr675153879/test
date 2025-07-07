package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.ConfirmSignDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiSignDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiSignLeft;

import java.util.List;
import java.util.Map;

/**
* 绩效签发 左侧固定 服务接口类
*/
public interface IKpiSignLeftService extends IService<KpiSignLeft> {

    KpiSignDTO signList(Long period);

    void confirmSign(ConfirmSignDTO dto);

    void confirmUnsign(ConfirmSignDTO dto);

    void importData(Long period, String overwriteFlag, List<Map<Integer, String>> list);

    void extend(ConfirmSignDTO dto);
}
