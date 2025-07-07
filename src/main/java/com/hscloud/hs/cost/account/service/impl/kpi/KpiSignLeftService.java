package com.hscloud.hs.cost.account.service.impl.kpi;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bestvike.linq.Linq;
import com.hscloud.hs.cost.account.constant.CacheConstants;
import com.hscloud.hs.cost.account.mapper.kpi.*;
import com.hscloud.hs.cost.account.model.dto.ConfirmSignDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiReportConfigIndexDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiSignDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiSignDataDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.model.entity.second.Attendance;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskCount;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiDeptUserIdVO;
import com.hscloud.hs.cost.account.utils.RedisUtil;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.service.kpi.IKpiSignLeftService;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
* 绩效签发 左侧固定 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = false)
@Slf4j
public class KpiSignLeftService extends ServiceImpl<KpiSignLeftMapper, KpiSignLeft> implements IKpiSignLeftService {
    @Autowired
    private KpiSignLeftMapper kpiSignLeftMapper;
    @Autowired
    private KpiSignHeadMapper kpiSignHeadMapper;
    @Autowired
    private KpiSignRightMapper kpiSignRightMapper;
    @Autowired
    private KpiItemMapper kpiItemMapper;
    @Autowired
    private KpiDictItemMapper kpiDictItemMapper;
    @Autowired
    private KpiDictMapper kpiDictMapper;
    @Autowired
    private KpiUserFactorMapper kpiUserFactorMapper;
    @Autowired
    private KpiConfigMapper kpiConfigMapper;
    @Autowired
    private KpiCalculateMapper kpiCalculateMapper;
    @Autowired
    private KpiItemResultCopyMapper kpiItemResultCopyMapper;
    @Autowired
    private  RedisUtil redisUtil;
    @Autowired
    private KpiSignHeadCopyMapper kpiSignHeadCopyMapper;
    @Autowired
    private KpiReportConfigService kpiReportConfigService;

    @Override
    public KpiSignDTO signList(Long period) {
        if (period == null) {
            throw new BizException("周期不能为空");
        }
        KpiConfig config = kpiConfigMapper.selectOne(
                new QueryWrapper<KpiConfig>()
                        .eq("period", period)
        );
        if (config == null){
            throw new BizException("周期不存在");
        }
        if ("Y".equals(config.getSignFlag())){
            return signedList(period.toString());
        }else {
            return unSignList(period.toString(),config.getTaskChildId());
        }
    }

    @Override
    public void confirmSign(ConfirmSignDTO dto) {
        KpiConfig config = kpiConfigMapper.selectOne(
                new QueryWrapper<KpiConfig>()
                        .eq("period", dto.getPeriod())
        );
        if (config == null){
            throw new BizException("周期不存在");
        }
        config.setSignFlag("Y");
        kpiConfigMapper.updateById(config);

        /*if (dto.getLefts() != null && !dto.getLefts().isEmpty()) {
            kpiSignLeftMapper.updateDelFlag(new QueryWrapper<KpiSignLeft>().eq("period",dto.getPeriod()));
            kpiSignLeftMapper.insertBatchSomeColumn(dto.getLefts());
        }*/

        List<KpiSignHeadCopy> copys = kpiSignHeadMapper.getList(
                new QueryWrapper<KpiSignHead>()
                        .eq("status","0")
                        .eq("del_flag", "0")
        );
        if (!copys.isEmpty()) {
            copys.forEach(x->x.setPeriod(dto.getPeriod()));
            kpiSignHeadCopyMapper.insertBatchSomeColumn(copys);
        }


    }

    @Override
    public void confirmUnsign(ConfirmSignDTO dto) {
        KpiConfig config = kpiConfigMapper.selectOne(
                new QueryWrapper<KpiConfig>()
                        .eq("period", dto.getPeriod())
        );
        if (config == null){
            throw new BizException("周期不存在");
        }
        config.setSignFlag("N");
        kpiConfigMapper.updateById(config);

        kpiSignLeftMapper.updateDelFlag(new QueryWrapper<KpiSignLeft>().eq("period",dto.getPeriod()));
        kpiSignHeadCopyMapper.delete(
                new QueryWrapper<KpiSignHeadCopy>()
                        .eq("period",dto.getPeriod())
        );
    }

    @Override
    public void importData(Long period,String overwriteFlag, List<Map<Integer, String>> list) {

        List<KpiSignHead> heads = kpiSignHeadMapper.selectList(
                new QueryWrapper<KpiSignHead>()
                        .eq("source",1)
                        .eq("status","0")
                        .eq("del_flag", "0")
        );
        if ("1".equals(overwriteFlag)){
            kpiSignRightMapper.updateDelFlag(new QueryWrapper<KpiSignRight>()
                    .eq("period", period)
                    .in("head_id",Linq.of(heads).select(x->x.getId()).toList()));
        }

        List<KpiSignRight> rights = kpiSignRightMapper.selectList(
                new QueryWrapper<KpiSignRight>()
                        .eq("period", period)
                        .eq("del_flag","0")
        );

        List<SysUser> users = kpiCalculateMapper.getUsers(SecurityUtils.getUser().getTenantId());
        List<KpiReportConfigImport> imports = new ArrayList<>();
        Map<Integer, String> top = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            KpiReportConfigImport entity = new KpiReportConfigImport();
            int finalI = i;
            Map<Long,BigDecimal> map = new HashMap<>();
            top.forEach((key, value) -> {
                if ("人员姓名".equals(value) || "姓名".equals(value)) {
                    entity.setUserName(list.get(finalI).get(key));
                }
                for (KpiSignHead head : heads) {
                    if (head.getName().equals(value)){
                        String s = list.get(finalI).get(key);
                        if (!StringUtil.isNullOrEmpty(s)) {
                            if (!NumberUtil.isNumber(s)) {
                                throw new BizException("第" + finalI + "行" + value + "不是数字");
                            } else {
                                map.put(head.getId(), new BigDecimal(s));
                            }
                        }
                    }
                }
            });
            entity.setMap(map);
            SysUser user = Linq.of(users).firstOrDefault(x -> x.getUsername().equals(entity.getUserName()));
            if (user!=null){
                entity.setUserId(user.getUserId());
            }
            imports.add(entity);
        }
        System.out.println(imports);
        System.out.println(1);
        for (KpiReportConfigImport anImport : imports) {
            anImport.getMap().forEach((key,value)->{
                KpiSignRight right = Linq.of(rights).firstOrDefault(x -> x.getUserId().equals(anImport.getUserId())
                        && key.equals(x.getHeadId()));
                if (right!=null){
                    KpiSignHead head = Linq.of(heads).firstOrDefault(x -> right.getHeadId().equals(x.getId()));
                    right.setValueB(value);

                    //1直接输入 2 计算 a*b
                    if (head.getCountType() == 1){
                        right.setValue(value);
                    }else{
                        right.setValueA(head.getPrice());
                        if (right.getValueA() != null && right.getValueB() != null) {
                            right.setValue(right.getValueA().multiply(right.getValueB()));
                        }

                    }
                    kpiSignRightMapper.updateById(right);
                }else{
                    KpiSignRight r = new KpiSignRight();
                    r.setHeadId(key);
                    r.setUserId(anImport.getUserId());
                    r.setPeriod(period);
                    r.setValueB(value);
                    r.setTenantId(SecurityUtils.getUser().getTenantId());

                    KpiSignHead head = Linq.of(heads).firstOrDefault(x -> key.equals(x.getId()));
                    //1直接输入 2 计算 a*b
                    if (head.getCountType() == 1){
                        r.setValue(value);
                    }else{
                        r.setValueA(head.getPrice());
                        if (right.getValueA() != null && right.getValueB() != null) {
                            r.setValue(r.getValueA().multiply(r.getValueB()));
                        }
                    }
                    kpiSignRightMapper.insert(r);
                }
            });
        }
    }

    @Override
    public void extend(ConfirmSignDTO dto) {
        //继承上月且手工上报
        List<KpiSignHead> heads = kpiSignHeadMapper.selectList(
                new QueryWrapper<KpiSignHead>()
                        .eq("del_flag", "0")
                        .eq("status", "0")
                        .eq("extend_last_month",1)
                        .eq("source",1)
        );
        if (heads == null || heads.isEmpty()){
            return;
        }
        List<KpiSignRight> rights = kpiSignRightMapper.selectList(
                new QueryWrapper<KpiSignRight>()
                        .in("head_id", Linq.of(heads).select(x -> x.getId()).toList())
                        .eq("del_flag", "0")
                        .eq("period", kpiReportConfigService.getLastPeriod(dto.getPeriod()))
        );
        List<KpiSignRight> adds = new ArrayList<>();
        rights.forEach(r->{
            KpiSignHead head = Linq.of(heads).firstOrDefault(z -> z.getId().equals(r.getHeadId()));
            r.setPeriod(dto.getPeriod());
            //1直接输入 2 计算 a*b
            r.setValueA(head.getPrice());
            if (r.getValueA() !=null && r.getValueB() != null){
                r.setValue(r.getValueA().multiply(r.getValueB()));
            }
            r.setId(null);
            adds.add(r);
        });
        if (!adds.isEmpty()){
            kpiSignRightMapper.updateDelFlag(new QueryWrapper<KpiSignRight>().eq("period",dto.getPeriod())
                    .in("head_id", Linq.of(heads).select(x -> x.getId()).toList()));
            kpiSignRightMapper.insertBatchSomeColumn(adds);
        }
    }

    public KpiSignDTO signedList(String period) {
        KpiSignDTO rt = new KpiSignDTO();
        List<KpiSignLeft> lefts = kpiSignLeftMapper.selectList(new QueryWrapper<KpiSignLeft>()
                .eq("period", period));
        List<KpiSignHead> heads = kpiSignHeadCopyMapper.getList(
                new QueryWrapper<KpiSignHeadCopy>()
                        .eq("status","0")
                        .eq("period",period)
        );
        List<KpiDict> dicts = kpiDictMapper.selectList(
                new QueryWrapper<KpiDict>()
                        .eq("kpi_sign", "1")
        );
        List<KpiDictItem> dict = kpiDictItemMapper.selectList(
                new QueryWrapper<KpiDictItem>()
                        .in("dict_type", Linq.of(dicts).select(x->x.getDictType()).toList())
        );
        List<KpiUserFactor> userFactors = kpiUserFactorMapper.selectList(
                new QueryWrapper<KpiUserFactor>()
                        .in("dict_type", Linq.of(dicts).select(x->x.getDictType()).toList())
        );
        List<KpiSignRight> rights = kpiSignRightMapper.selectList(
                new QueryWrapper<KpiSignRight>()
                        .eq("del_flag","0")
                        .eq("period", period.replace("-", ""))
        );
        List<KpiSignDataDTO> data = Linq.of(lefts).select(left -> {
            List<KpiSignRight> list = Linq.of(rights).where(x -> left.getUserId().equals(x.getUserId())).toList();

            //用工性质
            List<JSONObject> jsons = new ArrayList<>();
            for (KpiDict kpiDict : dicts) {
                KpiUserFactor kpiUserFactor = Linq.of(userFactors).firstOrDefault(x -> left.getUserId().equals(x.getUserId()) && x.getDictType().equals(kpiDict.getDictType()));
                if (kpiUserFactor!=null){
                    KpiDictItem kpiDictItem = Linq.of(dict).firstOrDefault(x -> x.getItemCode().equals(kpiUserFactor.getItemCode()));
                    if (kpiDictItem!=null){
                        //left.setUserType(kpiDictItem.getLabel());
                        JSONObject json = new JSONObject();
                        json.put("dictType",kpiDict.getDictType());
                        json.put("name",kpiDict.getDescription());
                        json.put("value",kpiDictItem.getLabel());
                        jsons.add(json);
                    }
                }
            }
            KpiSignDataDTO dto = new KpiSignDataDTO();
            dto.setLeft(left);
            dto.setRights(list);
            dto.setDicts(jsons);
            return dto;
        }).toList();

        for (KpiDict d : dicts) {
            KpiSignHead head = new KpiSignHead();
            head.setName(d.getDescription());
            head.setHeadType("dict");
            heads.add(head);
        }
        rt.setHead(heads);
        rt.setData(data);
        return rt;
    }


    public KpiSignDTO unSignList(String period,Long taskChildId) {
        KpiSignDTO rt = new KpiSignDTO();

        List<KpiCalculate> firsts = kpiSignLeftMapper.first(period);
        List<KpiCalculate> cas = Linq.of(firsts).where(x -> !StringUtil.isNullOrEmpty(x.getPlanCode())
                && x.getUserId() != null && ("1".equals(x.getImputationType()) || "0".equals(x.getImputationType()))).toList();
        if (period.length() == 6){
            period = period.substring(0,4)+'-'+period.substring(4,6);
        }
        List<UnitTaskCount> seconds = kpiSignLeftMapper.second(period);
        List<KpiSignHead> heads = kpiSignHeadMapper.selectList(
                new QueryWrapper<KpiSignHead>()
                        .eq("status","0")
                        .eq("del_flag", "0")
        );
        /*List<KpiSignRight> rights = kpiSignRightMapper.selectList(
                new QueryWrapper<KpiSignRight>()
                        .eq("period", period.replace("-", ""))
        );*/
        List<KpiSignRight> rights = getRights( heads,period.replace("-", ""),taskChildId,cas);

        List<KpiDict> dicts = kpiDictMapper.selectList(
                new QueryWrapper<KpiDict>()
                        .eq("kpi_sign", "1")
        );
        List<KpiDictItem> dict;
        List<KpiUserFactor> userFactors;
        if (!dicts.isEmpty()) {
            dict = kpiDictItemMapper.selectList(
                    new QueryWrapper<KpiDictItem>()
                            .in("dict_type", Linq.of(dicts).select(x -> x.getDictType()).toList())
            );
            userFactors = kpiUserFactorMapper.selectList(
                    new QueryWrapper<KpiUserFactor>()
                            .in("dict_type", Linq.of(dicts).select(x -> x.getDictType()).toList())
            );
        } else {
            dict = new ArrayList<>();
            userFactors = new ArrayList<>();
        }
        List<KpiSignLeft> lefts = new ArrayList<>();
        List<KpiSignDataDTO> data = Linq.of(cas)
                .groupBy(x -> new KpiDeptUserIdVO(x.getUserId(), x.getUserName(), x.getPeriod())).select(t -> {
                    KpiSignLeft left = new KpiSignLeft();
                    left.setUserName(t.getKey().getDeptName());
                    left.setUserId(t.getKey().getDeptId());
                    left.setPeriod(t.getKey().getPeriod());
                    left.setTenantId(SecurityUtils.getUser().getTenantId());
                    //一次分配
                    left.setFirstAmount(Linq.of(t.toList()).sumDecimal(z -> z.getValue()));
                    List<KpiCalculate> dept = Linq.of(cas).where(a -> left.getUserId().equals(a.getUserId())
                            && a.getDeptId() != null).toList();
                    if (dept!= null && !dept.isEmpty()) {
                        left.setDeptId(String.join(",",Linq.of(dept).select(b -> b.getDeptId().toString()).distinct()));
                        left.setDeptName(String.join(",",Linq.of(dept).select(b -> b.getDeptName()).distinct().toList()));
                    }

                    //二次分配
                    left.setSecondAmount(Linq.of(seconds).where(x->x.getUserId().equals(left.getUserId())).sumDecimal(x->x.getAmt()));
                    left.setAmount(left.getFirstAmount().add(left.getSecondAmount()));
                    //右侧
                    List<KpiSignRight> list = Linq.of(rights).where(x -> left.getUserId().equals(x.getUserId())).toList();
                    BigDecimal sum = Linq.of(list).sumDecimal(x -> x.getValue());
                    left.setSum(sum.add(left.getAmount()));

                    //用工性质
                    List<JSONObject> jsons = new ArrayList<>();
                    for (KpiDict kpiDict : dicts) {
                        KpiUserFactor kpiUserFactor = Linq.of(userFactors).firstOrDefault(x -> left.getUserId().equals(x.getUserId()) && x.getDictType().equals(kpiDict.getDictType()));
                        if (kpiUserFactor!=null){
                            KpiDictItem kpiDictItem = Linq.of(dict).firstOrDefault(x -> x.getItemCode().equals(kpiUserFactor.getItemCode()));
                            if (kpiDictItem!=null){
                                //left.setUserType(kpiDictItem.getLabel());
                                JSONObject json = new JSONObject();
                                json.put("dictType",kpiDict.getDictType());
                                json.put("name",kpiDict.getDescription());
                                json.put("value",kpiDictItem.getLabel());
                                jsons.add(json);
                            }
                        }
                    }

                    lefts.add(left);
                    KpiSignDataDTO dto = new KpiSignDataDTO();
                    dto.setLeft(left);
                    dto.setRights(list);
                    dto.setDicts(jsons);
                    return dto;
                }).toList();

        for (KpiDict d : dicts) {
            KpiSignHead head = new KpiSignHead();
            head.setName(d.getDescription());
            head.setHeadType("dict");
            head.setDictType(d.getDictType());
            heads.add(head);
        }
        if (!lefts.isEmpty()){
            kpiSignLeftMapper.delete(new QueryWrapper<KpiSignLeft>()
                    .eq("period", period.replace("-","")));
            kpiSignLeftMapper.insertBatchSomeColumn(lefts);
        }
        rt.setData(data);
        rt.setHead(heads);
        return rt;
    }

    private List<KpiSignRight> getRights(List<KpiSignHead> heads, String period2,Long taskChildId,List<KpiCalculate> cas) {
        long period = Long.parseLong(period2);
        //需要计算的
        List<KpiSignHead> needs = Linq.of(heads).where(x ->  x.getSource() == 2
                && (x.getLastDate() == null || DateUtil.compare(x.getLastDate(), x.getUpdatedDate()) <= 0)).toList();

        if (!heads.isEmpty()) {
            heads.removeAll(needs);
            List<KpiItem> items = kpiItemMapper.selectList(
                    new QueryWrapper<KpiItem>()
                            .eq("1", "1")
                            .in(!heads.isEmpty(), "code", Linq.of(heads).select(x -> x.getCode()).toList())
            );

            for (KpiSignHead head : heads) {
                KpiItem item = Linq.of(items).firstOrDefault(x -> x.getCode().equals(head.getCode()));
                if (item != null && item.getExtDate() != null && DateUtil.compare(item.getExtDate(), head.getLastDate()) >= 0) {
                    needs.add(head);
                }
            }
        }

        if (needs == null || needs.isEmpty()){
            return kpiSignRightMapper.selectList(
                    new QueryWrapper<KpiSignRight>()
                            .eq("del_flag","0")
                            .in(!heads.isEmpty(),"head_id",Linq.of(heads).select(x->x.getId()).toList())
                            .eq("period",period)
            );
        }

        String key = CacheConstants.COST_SIGN_RIGHT + ":" + period;
        String lockValue = UUID.randomUUID().toString();
        Boolean lockAcquired = redisUtil.setLock(key, lockValue, 5L, TimeUnit.MINUTES);
        try {
            if (!lockAcquired) {
                throw new BizException("签发列表生成中 请勿重复操作");
            }
            List<KpiItemResultCopy> list = kpiItemResultCopyMapper.selectList(
                    new QueryWrapper<KpiItemResultCopy>()
                            .eq("task_child_id", taskChildId)
                            .in("code", Linq.of(needs).where(x -> x.getSource() == 2).select(x -> x.getCode()).toList())
            );
            List<Long> userIds = new ArrayList<>();
            if (cas!=null && !cas.isEmpty()) {
                userIds = Linq.of(cas).select(x -> x.getUserId()).distinct().toList();
            }else{
                List<SysUser> users = kpiCalculateMapper.getUsers(SecurityUtils.getUser().getTenantId());
                userIds = Linq.of(users).select(x -> x.getUserId()).distinct().toList();
            }
            List<KpiSignRight> rights = new ArrayList<>();
            for (KpiSignHead head : needs) {
                for (Long userId : userIds) {
                    KpiSignRight right = new KpiSignRight();
                    right.setUserId(userId);
                    right.setTenantId(SecurityUtils.getUser().getTenantId());
                    right.setHeadId(head.getId());
                    right.setPeriod(period);
                    if (head.getSource() == 2){
                        KpiItemResultCopy first = Linq.of(list).firstOrDefault(x -> userId.equals(x.getUserId()) && x.getCode().equals(head.getCode()));
                        BigDecimal bigDecimal = new BigDecimal(0);
                        if (first != null) {
                            bigDecimal=first.getValue();
                        }
                        if (head.getCountType() == 2) {
                            right.setValueA(head.getPrice());
                            right.setValueB(bigDecimal);
                            if (right.getValueA() != null) {
                                right.setValue(right.getValueA().multiply(right.getValueB()));
                            }
                        }else {
                            right.setValue(bigDecimal);
                        }

                    }
                    rights.add(right);
                }
                head.setLastDate(DateUtils.addMinutes(new Date(),1));
                kpiSignHeadMapper.updateById(head);
            }
            kpiSignRightMapper.updateDelFlag(
                    new QueryWrapper<KpiSignRight>()
                            .eq("del_flag","0")
                            .in("head_id",Linq.of(needs).select(x->x.getId()).toList())
                            .eq("period",period)
            );
            kpiSignRightMapper.insertBatchSomeColumn(rights);
            return rights;
        }finally {
            if (lockAcquired) {
                redisUtil.unLock(key, lockValue);
            }
        }
    }

}
