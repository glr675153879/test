package com.hscloud.hs.cost.account.service.impl.kpi;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.enums.kpi.CaliberEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.MemberEnum;
import com.hscloud.hs.cost.account.mapper.kpi.KpiIndexFormulaMapper;
import com.hscloud.hs.cost.account.mapper.kpi.KpiIndexFormulaObjMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiIndexFormulaPlanVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiIndexFormulaVO;
import com.hscloud.hs.cost.account.service.kpi.IKpiIndexFormulaObjService;
import com.hscloud.hs.cost.account.service.kpi.IKpiIndexFormulaService;
import com.hscloud.hs.cost.account.utils.kpi.Convert;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
* 指标公式对象表 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class KpiIndexFormulaObjService extends ServiceImpl<KpiIndexFormulaObjMapper, KpiIndexFormulaObj> implements IKpiIndexFormulaObjService {
    @Autowired
    private KpiIndexFormulaObjMapper kpiIndexFormulaObjMapper;

    public void insertBatchSomeColumn(List<KpiIndexFormulaObj> list) {
        kpiIndexFormulaObjMapper.insertBatchSomeColumn(list);
    }
}
