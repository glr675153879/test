package com.hscloud.hs.cost.account.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.CostAccountUnitMapper;
import com.hscloud.hs.cost.account.mapper.CostDocNRelationMapper;
import com.hscloud.hs.cost.account.model.dto.BindDocNDto;
import com.hscloud.hs.cost.account.model.dto.CommonDTO;
import com.hscloud.hs.cost.account.model.dto.listDocNRelationDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountUnit;
import com.hscloud.hs.cost.account.model.entity.CostDocNRelation;
import com.hscloud.hs.cost.account.model.vo.DictItemVo;
import com.hscloud.hs.cost.account.model.vo.DocNRelationListVo;
import com.hscloud.hs.cost.account.service.CostAccountUnitService;
import com.hscloud.hs.cost.account.service.ICostDocNRelationService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 医护对应组Service接口实现
 * @author banana
 * @create 2023-09-11 14:38
 */
@Service
public class CostDocNRelationServiceImpl extends ServiceImpl<CostDocNRelationMapper, CostDocNRelation> implements ICostDocNRelationService {

    @Autowired
    private CostAccountUnitMapper costAccountUnitMapper;

    @Autowired
    private CostAccountUnitService costAccountUnitService;

    @Autowired
    private CostDocNRelationMapper costDocNRelationMapper;

    //医护对应关系绑定
    @Override
    public void bindDocNRelation(BindDocNDto input) {

        //判断医生组科室单元id入参存在 和 是否 为医生组
        Optional<CostAccountUnit> doc = costAccountUnitMapper.selectList(new LambdaQueryWrapper<CostAccountUnit>()
                .eq(CostAccountUnit::getId, input.getDocAccountGroupId())).stream().findFirst();
        if(doc.isPresent()){
            String accountGroupCode = doc.get().getAccountGroupCode();
            DictItemVo docDictItem = JSON.parseObject(accountGroupCode, DictItemVo.class);
            //TODO 字典值写死
            if(!"HSDX001".equals(docDictItem.getValue()))throw new BizException("当前id不为医生组科室单元");
        }
        else throw new BizException("当前医生组科室单元id不存在");

        //删除旧的医生科室单元的关联
        baseMapper.delete(new LambdaQueryWrapper<CostDocNRelation>()
                .eq(CostDocNRelation::getDocAccountGroupId, input.getDocAccountGroupId()));

        for(CommonDTO list : input.getNurseAccountGroupList()){
            //判断护理组科室单元id入参存在 和 是否 为护理组
            Optional<CostAccountUnit> nurse = costAccountUnitMapper.selectList(new LambdaQueryWrapper<CostAccountUnit>()
                    .eq(CostAccountUnit::getId, Long.valueOf(list.getId()))).stream().findFirst();
            if(nurse.isPresent()){
                String accountGroupCode = nurse.get().getAccountGroupCode();
                DictItemVo nurseDictItem = JSON.parseObject(accountGroupCode, DictItemVo.class);
                //TODO 字典值写死
                if(!"HSDX002".equals(nurseDictItem.getValue()))throw new BizException("当前id不为护理组科室单元");
            }
            else throw new BizException("当前护理组科室单元id不存在");

            //绑定新的医生科室单元id 和 护理组科室单元id
            CostDocNRelation costDocNRelation = new CostDocNRelation();
            costDocNRelation.setDocAccountGroupId(input.getDocAccountGroupId());
            costDocNRelation.setNurseAccountGroupId(Long.valueOf(list.getId()));
            baseMapper.insert(costDocNRelation);
        }
    }


    /**
     * 获取医护对应组列表
     * @param input 入参
     * @return 医护对应关系
     */
    @Override
    public IPage<DocNRelationListVo> listDocNRelation(listDocNRelationDto input){
        //获取所有医生科室单元信息单元信息
        IPage<CostAccountUnit> collects = costDocNRelationMapper.listDocNRelation(new Page(input.getCurrent(), input.getSize()),
                input.getDocName(), input.getNurseName());

        List<CostAccountUnit> docList = collects.getRecords();

        //声明出参
        IPage<DocNRelationListVo> ret = new Page<>(input.getCurrent(), input.getSize());

        List<DocNRelationListVo> queryVos = new ArrayList<>();

        //封装输出数据
        for(CostAccountUnit doc : docList){
            DocNRelationListVo record = new DocNRelationListVo();
            CommonDTO commonDTO = new CommonDTO();
            commonDTO.setId(doc.getId().toString());
            commonDTO.setName(doc.getName());
            record.setDocInfo(commonDTO);
            record.setNurseInfo(new ArrayList<>());

            //查询当前医生对应的护理
            List<CostDocNRelation> docNurses = baseMapper.selectList(new LambdaQueryWrapper<CostDocNRelation>()
                    .eq(CostDocNRelation::getDocAccountGroupId, doc.getId()));
            if(!CollectionUtils.isEmpty(docNurses)){
                for(CostDocNRelation docNurse : docNurses){
                    Optional<CostAccountUnit> nurse = costAccountUnitMapper.selectList(new LambdaQueryWrapper<CostAccountUnit>()
                            .eq(CostAccountUnit::getId, docNurse.getNurseAccountGroupId())).stream().findFirst();
                    if(nurse.isPresent()){
                        commonDTO = new CommonDTO();
                        commonDTO.setId(nurse.get().getId().toString());
                        commonDTO.setName(nurse.get().getName());
                        record.getNurseInfo().add(commonDTO);
                    }
                }
            }
            queryVos.add(record);
        }
        //所有记录信息
        ret.setRecords(queryVos);
        //当前页条数
        ret.setSize(input.getSize());
        //总数据条数
        ret.setTotal(collects.getTotal());

        return ret;
    }
}
