package com.hscloud.hs.cost.account.service.impl.kpi;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.enums.kpi.CodePrefixEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.IndexTypeEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.MemberEnum;
import com.hscloud.hs.cost.account.mapper.kpi.KpiIndexMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiIndexDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiIndexEnableDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiIndexListDto;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiIndexListVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiIndexVO;
import com.hscloud.hs.cost.account.service.kpi.CommCodeService;
import com.hscloud.hs.cost.account.service.kpi.IKpiIndexService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import groovy.lang.Lazy;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
* 指标表 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class KpiIndexService extends ServiceImpl<KpiIndexMapper, KpiIndex> implements IKpiIndexService {

    @Autowired
    private KpiIndexMapper kpiIndexMapper;
    @Autowired
    private CommCodeService commCodeService;
    @Autowired
    @Lazy
    private KpiIndexFormulaService kpiIndexFormulaService;
    @Autowired
    private KpiMemberService kpiMemberService;
    @Autowired
    private KpiIndexFormulaObjService kpiIndexFormulaObjService;

    @Override
    public Long saveOrUpdateKpiIndex(KpiIndexDto dto) {
        KpiIndex kpiIndex = BeanUtil.copyProperties(dto, KpiIndex.class);
        LambdaQueryWrapper<KpiIndex> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KpiIndex::getName, dto.getName()).eq(KpiIndex::getDelFlag, "0");
        KpiIndex one = this.getOne(queryWrapper);
        if (dto.getId() == null && one != null && one.getName().equals(dto.getName())) {
            throw new BizException("该指标名称已存在");
        }
        if (dto.getId() != null && one != null && one.getName().equals(dto.getName()) && !one.getId().equals(dto.getId())){
            throw new BizException("该指标名称已存在");
        }
        if (dto.getId() == null || dto.getId() == 0L){
            kpiIndex.setStatus("0");
            kpiIndex.setDelFlag("0");
            if (dto.getType().equals(IndexTypeEnum.ALLOCATION.getType())){
                kpiIndex.setCode(commCodeService.commCode(CodePrefixEnum.ALLOCATION));
            }else {
                kpiIndex.setCode(commCodeService.commCode(CodePrefixEnum.INDEX));
            }
        }else {
            KpiIndex byId = getById(dto.getId());
//            在有公式的情况下，口径颗粒度不允许修改
            if (StringUtils.isNotBlank(dto.getCaliber()) && !byId.getCaliber().equals(dto.getCaliber())){
                List<KpiIndexFormula> list = kpiIndexFormulaService.list(new LambdaQueryWrapper<KpiIndexFormula>().eq(KpiIndexFormula::getIndexCode, one.getCode()));
                if (CollectionUtil.isNotEmpty(list)){
                    throw new BizException("在有公式的情况下，口径颗粒度不允许修改");
                }
            }
        }
        saveOrUpdate(kpiIndex);
        return kpiIndex.getId();
        //提取指标下所有公式的指标+指标项+分摊项存入关联表方便套环查询，已在公式保存实现

    }

    //判断指标是否有被引用
    Boolean isOnUse(KpiIndex kpiIndex){
        //本身是否有公式
        List<KpiIndexFormula> list = kpiIndexFormulaService.list(new LambdaQueryWrapper<KpiIndexFormula>().eq(KpiIndexFormula::getIndexCode, kpiIndex.getCode()));
        if (CollectionUtil.isNotEmpty(list)){
            return true;
        }
//        指标中是否有被引用
        List<Long> collect = list(new LambdaQueryWrapper<KpiIndex>().eq(KpiIndex::getStatus, "0").eq(KpiIndex::getDelFlag, "0")).stream().map(o -> o.getId()).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(collect)){
            return false;
        }

        List<KpiMember> collect1 = kpiMemberService.list(new LambdaQueryWrapper<KpiMember>()
                .eq(KpiMember::getMemberType, MemberEnum.FORMULA_ITEM.getType())
                .in(KpiMember::getHostId, collect)
        ).stream().collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(collect1)){
            return true;
        }
        return false;
    }

    @Override
    public void enable(KpiIndexEnableDto dto) {
        KpiIndex byId = getById(dto.getId());
        byId.setStatus(dto.getStatus());
        updateById(byId);
        //相关方案禁用

    }


    void getIndexByDependency(List<String> indexCodes, List<String> allCodes){
        if (CollectionUtil.isEmpty(indexCodes)){
            List<KpiMember> list = kpiMemberService.list(new LambdaQueryWrapper<KpiMember>().in(KpiMember::getMemberCode, indexCodes).eq(KpiMember::getMemberType, MemberEnum.FORMULA_ITEM.getType()));
            allCodes.addAll(list.stream().map(o->o.getHostCode()).collect(Collectors.toList()));
            getIndexByDependency(list.stream().map(o->o.getHostCode()).collect(Collectors.toList()), allCodes);
        }
    }

    @Override
    public List<KpiIndexListVO> list(KpiIndexListDto dto) {
        return kpiIndexMapper.getList(dto);
    }

    @Override
    public IPage<KpiIndexListVO> getPage(KpiIndexListDto input) {
        return kpiIndexMapper.getPate(new Page<>(input.getCurrent(), input.getSize()), input);
    }

    @Override
    public void del(Long id) {
        KpiIndex byId = getById(id);
        byId.setDelFlag("1");
        updateById(byId);

        try {
            List<KpiIndexFormula> list = kpiIndexFormulaService.list(new LambdaQueryWrapper<KpiIndexFormula>().eq(KpiIndexFormula::getIndexCode, byId.getCode()));
            kpiIndexFormulaService.updateDelFlag(list.stream().map(o->o.getId()).collect(Collectors.toList()));
            kpiIndexFormulaObjService.remove(new LambdaQueryWrapper<KpiIndexFormulaObj>().in(KpiIndexFormulaObj::getFormulaId, list.stream().map(o->o.getId()).collect(Collectors.toList())));
            kpiMemberService.remove(new LambdaQueryWrapper<KpiMember>().in(KpiMember::getHostId, list.stream().map(o->o.getId()).collect(Collectors.toList())).eq(KpiMember::getMemberType, MemberEnum.FORMULA_ITEM.getType()));
        }catch (Exception e){

        }
    }

    @Override
    public KpiIndexVO getInfo(Long id) {
        KpiIndex byId = getById(id);
        KpiIndexVO kpiIndexVO = BeanUtil.copyProperties(byId, KpiIndexVO.class);
        Boolean onUse = isOnUse(byId);
        if (onUse){
            kpiIndexVO.setAllowModify(1);
        }else {
            kpiIndexVO.setAllowModify(0);
        }
        return kpiIndexVO;
    }
}
