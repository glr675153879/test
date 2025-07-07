package com.hscloud.hs.cost.account.service.dataReport;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.budget.model.vo.export.ImportErrListVo;
import com.hscloud.hs.budget.model.vo.export.ImportErrVo;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.dataReport.OdsHisUnidrgswedDboDrgsInfo;
import com.hscloud.hs.cost.account.model.vo.dataReport.DownloadTemplateRwVo;
import com.hscloud.hs.cost.account.model.vo.dataReport.ExportRwVo;
import org.springframework.validation.BindingResult;

import java.util.List;

/**
* RW值信息表 服务接口类
*/
public interface IDrgsInfoService extends IService<OdsHisUnidrgswedDboDrgsInfo> {

    List<OdsHisUnidrgswedDboDrgsInfo> listData(QueryWrapper<OdsHisUnidrgswedDboDrgsInfo> wrapper);

    Integer getRwJobHandler(String dt, String type);

    List<DownloadTemplateRwVo> downloadTemplateRw(PageRequest<OdsHisUnidrgswedDboDrgsInfo> pr);

    ImportErrVo uploadFileRw(List<DownloadTemplateRwVo> excelVOList, String dt, String type,
                                String continueFlag, String overwriteFlag, BindingResult bindingResult);

    List<ExportRwVo> exportRw(String dt, String type);

    List<ImportErrListVo> exportErrLog(String dt);
}
