package com.hscloud.hs.cost.account.service.impl.dataReport;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.budget.model.vo.export.ImportErrListVo;
import com.hscloud.hs.budget.model.vo.export.ImportErrVo;
import com.hscloud.hs.cost.account.constant.CacheConstants;
import com.hscloud.hs.cost.account.constant.enums.YesNoEnum;
import com.hscloud.hs.cost.account.mapper.dataReport.DrgsInfoMapper;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.dataReport.OdsHisUnidrgswedDboDrgsInfo;
import com.hscloud.hs.cost.account.model.vo.dataReport.DownloadTemplateRwVo;
import com.hscloud.hs.cost.account.model.vo.dataReport.ExportRwVo;
import com.hscloud.hs.cost.account.service.dataReport.IDrgsInfoService;
import com.hscloud.hs.cost.account.utils.DmoUtil;
import com.hscloud.hs.cost.account.utils.RedisUtil;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.excel.vo.ErrorMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* RW值信息表 服务实现类
*
*/
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DrgsInfoService extends ServiceImpl<DrgsInfoMapper, OdsHisUnidrgswedDboDrgsInfo> implements IDrgsInfoService {

    private final DmoUtil dmoUtil;
    private final RedisUtil redisUtil;

    @Override
    public List<OdsHisUnidrgswedDboDrgsInfo> listData(QueryWrapper<OdsHisUnidrgswedDboDrgsInfo> wrapper) {
        return list(wrapper);
    }

    /**
     * 每个月18号从数据中台获取上个月RW数据
     */
    // @XxlJob("getRwJobHandler")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer getRwJobHandler(String dt, String type) {
        log.info("getRWDataFromHub {} xxl job initiating...", dt);
        List<OdsHisUnidrgswedDboDrgsInfo> list = dmoUtil.rwList(dt);
        log.info("getRWDataFromHub {} xxl job done...", dt);
        for (OdsHisUnidrgswedDboDrgsInfo drgsInfo:list) {
            drgsInfo.setIsEditable(drgsInfo.getRw() == null);
        }
        super.remove(Wrappers.<OdsHisUnidrgswedDboDrgsInfo>lambdaQuery().eq(OdsHisUnidrgswedDboDrgsInfo::getDt, dt));
        saveBatch(list);
        return list.size();
    }

    @Override
    public List<DownloadTemplateRwVo> downloadTemplateRw(PageRequest<OdsHisUnidrgswedDboDrgsInfo> pr) {
        // 出参
        List<DownloadTemplateRwVo> rtn = new ArrayList<>();

        List<OdsHisUnidrgswedDboDrgsInfo> odsHisUnidrgswedDboDrgsInfos = list(pr.getWrapper());
        odsHisUnidrgswedDboDrgsInfos.stream().forEach(r -> {
            DownloadTemplateRwVo downloadTemplateRwVo = new DownloadTemplateRwVo();
            BeanUtil.copyProperties(r, downloadTemplateRwVo);
            rtn.add(downloadTemplateRwVo);
        });
        return rtn;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportErrVo uploadFileRw(List<DownloadTemplateRwVo> excelVOList, String dt, String type, String continueFlag,
                                    String overwriteFlag, BindingResult bindingResult) {
        // 异常数据处理
        List<ErrorMessage> errorMessageList = (List<ErrorMessage>) bindingResult.getTarget();

        // 响应参数
        ImportErrVo rtnVo = new ImportErrVo();
        // 设置头
        rtnVo.setHead(getRwErrHead());
        rtnVo.setDetails(new ArrayList<>());

        // 初始数据校验
        checkRwExcelVOList(excelVOList, rtnVo);

        // 需要终止
        if("2".equals(continueFlag)) {
            if(rtnVo.getDetails() != null && !rtnVo.getDetails().isEmpty()) {
                return rtnVo;
            }
        }

        // 覆盖导入需要清空原始数据
        if("1".equals(overwriteFlag) && StrUtil.isNotBlank(dt)) {
            // 查询出相关的数据
            update(Wrappers.<OdsHisUnidrgswedDboDrgsInfo>lambdaUpdate()
                    .set(OdsHisUnidrgswedDboDrgsInfo::getRw, null)
                    .eq(OdsHisUnidrgswedDboDrgsInfo::getDt, dt)
                    .eq(OdsHisUnidrgswedDboDrgsInfo::getType, type)
                    .eq(OdsHisUnidrgswedDboDrgsInfo::getIsEditable, YesNoEnum.YES.getValue()));
        }

        // Rw全量数据预处理
        List<OdsHisUnidrgswedDboDrgsInfo> odsList = list(Wrappers.<OdsHisUnidrgswedDboDrgsInfo>lambdaQuery()
                .eq(OdsHisUnidrgswedDboDrgsInfo::getDt, dt)
                .eq(OdsHisUnidrgswedDboDrgsInfo::getType, type)
                .eq(OdsHisUnidrgswedDboDrgsInfo::getIsEditable, YesNoEnum.YES.getValue()));

        // 遍历有效数据，更新RW值
        for(int i = 0; i < excelVOList.size(); i ++) {
            DownloadTemplateRwVo downloadTemplateRwVo = excelVOList.get(i);
            try {
                List<OdsHisUnidrgswedDboDrgsInfo> matchList = matchRw(downloadTemplateRwVo, odsList);
                if(CollectionUtil.isEmpty(matchList)){ // 未匹配到数据
                    throw new BizException("未匹配到数据");
                } else if (matchList.size() > 1) { // 匹配到多条
                    throw new Exception("匹配到多条数据");
                } else {
                    OdsHisUnidrgswedDboDrgsInfo target = matchList.get(0);
                    target.setRw(downloadTemplateRwVo.getRw());
                    updateById(target);
                }
            } catch (Exception ex) {
                log.error("更新rw值发生异常：导入明细：{}", downloadTemplateRwVo, ex);
                String exMsg = ex instanceof BizException ? ((BizException)ex).getDefaultMessage() : "更新RW值发生异常";
                handleEx(downloadTemplateRwVo, exMsg, rtnVo);
                // 需要终止
                if("2".equals(continueFlag)) {
                    if(rtnVo.getDetails() != null && !rtnVo.getDetails().isEmpty()) {
                        return rtnVo;
                    }
                }
            }
        }

        // 将错误信息存入redis缓存中
        redisUtil.set(CacheConstants.RW_IMPORT_ERROR + dt, rtnVo, 30, TimeUnit.MINUTES);

        return rtnVo;
    }

    // 包装异常
    private void handleEx(DownloadTemplateRwVo downloadTemplateRwVo, String content, ImportErrVo rtnVo) {
        ImportErrListVo importErrListVo = new ImportErrListVo();
        importErrListVo.setLineNum(Math.toIntExact(downloadTemplateRwVo.getLineNum()) + 1);
        importErrListVo.setContent(content);
        List<String> data = new ArrayList<>();
        /*data.add(Objects.isNull(downloadTemplateRwVo.getId()) ? "" : downloadTemplateRwVo.getId().toString());*/
        data.add(Objects.isNull(downloadTemplateRwVo.getAccountId()) ? "" : downloadTemplateRwVo.getAccountId().toString());
        data.add(Objects.isNull(downloadTemplateRwVo.getAccountUnit()) ? "" : downloadTemplateRwVo.getAccountUnit().toString());
        data.add(StrUtil.isBlank(downloadTemplateRwVo.getRegCode()) ? "" : downloadTemplateRwVo.getRegCode().toString());
        data.add(Objects.isNull(downloadTemplateRwVo.getRw()) ? "" : downloadTemplateRwVo.getRw().toString());
        data.add(StrUtil.isBlank(downloadTemplateRwVo.getOutDeptName()) ? "" : downloadTemplateRwVo.getOutDeptName().toString());
        importErrListVo.setData(data);
        rtnVo.getDetails().add(importErrListVo);
    }


    // 根据 姓名 + 出院科室 + 住院天数匹配rw数据
    private List<OdsHisUnidrgswedDboDrgsInfo> matchRw(DownloadTemplateRwVo target, List<OdsHisUnidrgswedDboDrgsInfo> all) {

        String personName = target.getPersonName();
        String outDeptName = target.getOutDeptName();
        Integer zyts = target.getZyts();

        return all.stream().filter(odsHisUnidrgswedDboDrgsInfo -> Objects.equals(odsHisUnidrgswedDboDrgsInfo.getPersonName(), personName)
                        && Objects.equals(odsHisUnidrgswedDboDrgsInfo.getOutDeptName(), outDeptName)
                        && Objects.equals(odsHisUnidrgswedDboDrgsInfo.getZyts(), zyts))
                .collect(Collectors.toList());
    }


    private void checkRwExcelVOList(List<DownloadTemplateRwVo> excelVOList, ImportErrVo rtnVo) {
        // 需要移除的数据
        List<DownloadTemplateRwVo> removeExcelVOList = new ArrayList<>();

        for (DownloadTemplateRwVo downloadTemplateRwVo : excelVOList) {
            ImportErrListVo importErrListVo = new ImportErrListVo();
            importErrListVo.setLineNum(Math.toIntExact(downloadTemplateRwVo.getLineNum()) + 1);
            List<String> data = new ArrayList<>();
            /*data.add(Objects.isNull(downloadTemplateRwVo.getId()) ? "" : downloadTemplateRwVo.getId().toString());*/
            data.add(StrUtil.isBlank(downloadTemplateRwVo.getAccountId()) ? "" : downloadTemplateRwVo.getAccountId());
            data.add(StrUtil.isBlank(downloadTemplateRwVo.getAccountUnit()) ? "" : downloadTemplateRwVo.getAccountUnit());
            data.add(StrUtil.isBlank(downloadTemplateRwVo.getRegCode()) ? "" : downloadTemplateRwVo.getRegCode().toString());
            data.add(Objects.isNull(downloadTemplateRwVo.getRw()) ? "" : downloadTemplateRwVo.getRw().toString());
            data.add(StrUtil.isBlank(downloadTemplateRwVo.getOutDeptName()) ? "" : downloadTemplateRwVo.getOutDeptName().toString());
            importErrListVo.setData(data);

            // 校验id错误失败
            /*if(Objects.isNull(downloadTemplateRwVo.getId())) {
                importErrListVo.setContent("id不能为空");
                rtnVo.getDetails().add(importErrListVo);
                removeExcelVOList.add(downloadTemplateRwVo);
            }*/

            /*if(StrUtil.isBlank(downloadTemplateRwVo.getAccountId())) {
                importErrListVo.setContent("核算单元Id不能为空");
                rtnVo.getDetails().add(importErrListVo);
                removeExcelVOList.add(downloadTemplateRwVo);
            }

            if(StrUtil.isBlank(downloadTemplateRwVo.getAccountUnit())) {
                importErrListVo.setContent("核算单元名称不能为空");
                rtnVo.getDetails().add(importErrListVo);
                removeExcelVOList.add(downloadTemplateRwVo);
            }*/

            // 校验病案号失败
            /*if(StrUtil.isBlank(downloadTemplateRwVo.getRegCode())) {
                importErrListVo.setContent("病案号不能为空");
                rtnVo.getDetails().add(importErrListVo);
                removeExcelVOList.add(downloadTemplateRwVo);
            }*/

            // 姓名校验
            if(StrUtil.isBlank(downloadTemplateRwVo.getPersonName())) {
                importErrListVo.setContent("姓名不能为空");
                rtnVo.getDetails().add(importErrListVo);
                removeExcelVOList.add(downloadTemplateRwVo);
            }

            // 校验出院科室失败
            if(StrUtil.isBlank(downloadTemplateRwVo.getOutDeptName())) {
                importErrListVo.setContent("出院科室不能为空");
                rtnVo.getDetails().add(importErrListVo);
                removeExcelVOList.add(downloadTemplateRwVo);
            }

            // 校验住院天数失败
            if(Objects.equals(downloadTemplateRwVo.getZyts(), null)) {
                importErrListVo.setContent("出院天数不能为空");
                rtnVo.getDetails().add(importErrListVo);
                removeExcelVOList.add(downloadTemplateRwVo);
            }
        }
        excelVOList.removeAll(removeExcelVOList);
    }

    /**
     * 获取rw值导入错误日志
     * @return
     */
    private List<List<String>> getRwErrHead(){
        // 响应参数
        List<List<String>> rtnList = new ArrayList<>();

        List<String> lineNum = new ArrayList<>();
        lineNum.add("行数");
        rtnList.add(lineNum);

        /*List<String> id = new ArrayList<>();
        id.add("id");
        rtnList.add(id);*/

        List<String> accountId = new ArrayList<>();
        accountId.add("核算单元id");
        rtnList.add(accountId);

        List<String> accountUnit = new ArrayList<>();
        accountUnit.add("核算单元名称");
        rtnList.add(accountUnit);

        List<String> regCode = new ArrayList<>();
        regCode.add("病案号");
        rtnList.add(regCode);

        List<String> rw = new ArrayList<>();
        rw.add("rw值");
        rtnList.add(rw);

        List<String> outDeptName = new ArrayList<>();
        outDeptName.add("出院科室");
        rtnList.add(outDeptName);

        List<String> content = new ArrayList<>();
        content.add("错误说明");
        rtnList.add(content);

        return rtnList;
    }

    @Override
    public List<ExportRwVo> exportRw(String dt, String type) {

        // 声明出参
        List<ExportRwVo> rtn = new ArrayList<>();

        // 查询出相关的数据
        List<OdsHisUnidrgswedDboDrgsInfo> odsHisUnidrgswedDboDrgsInfos = list(Wrappers.<OdsHisUnidrgswedDboDrgsInfo>lambdaUpdate()
                .eq(OdsHisUnidrgswedDboDrgsInfo::getDt, dt)
                .eq(OdsHisUnidrgswedDboDrgsInfo::getType, type)
                .eq(OdsHisUnidrgswedDboDrgsInfo::getIsEditable, "1"));

        for (OdsHisUnidrgswedDboDrgsInfo odsHisUnidrgswedDboDrgsInfo : odsHisUnidrgswedDboDrgsInfos) {
            ExportRwVo exportRwVo = new ExportRwVo();
            BeanUtil.copyProperties(odsHisUnidrgswedDboDrgsInfo, exportRwVo);
            rtn.add(exportRwVo);
        }

        return rtn;
    }

    @Override
    public List<ImportErrListVo> exportErrLog(String dt) {
        List<ImportErrListVo> rtn = new ArrayList<>();
        ImportErrVo redisVo = (ImportErrVo) redisUtil.get(CacheConstants.RW_IMPORT_ERROR + dt);
        if(!Objects.isNull(redisVo)) {
            rtn = redisVo.getDetails();
        }
        return rtn;
    }

}
