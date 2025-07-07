package com.hscloud.hs.cost.account.service.dataReport;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.CostDataCollectionDto;
import com.hscloud.hs.cost.account.model.dto.dataReport.BatchAssignDto;
import com.hscloud.hs.cost.account.model.dto.dataReport.CostReportDetailInfoDto;
import com.hscloud.hs.cost.account.model.dto.dataReport.CostReportDetailRecordDto;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportRecord;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportRecordFileInfo;
import com.hscloud.hs.cost.account.model.vo.dataReport.AssignResultVo;
import com.pig4cloud.pigx.common.core.util.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 我的上报 服务接口类
 */
public interface ICostReportRecordService extends IService<CostReportRecord> {

    R<?> handleFileUpload(MultipartFile file, Long recordId);

    void initiateJobHandler();

    void assignJobHandler();

    R<?> editAndSave(List<CostReportDetailInfoDto> costReportDetailInfo);

    CostReportDetailRecordDto detailList(CostReportRecord costReportRecord);

    Object getRwData(CostDataCollectionDto input);

    R<?> downloadTemplate(Long unitTaskProjectId, HttpServletResponse response);

    Boolean uploadFile(CostReportRecordFileInfo fileInfo);

    Boolean removeErrorLog();

    Boolean approve(CostReportRecord costReportRecord);

    Boolean reject(CostReportRecord costReportRecord);

    Boolean submit(CostReportRecord costReportRecord);

    byte[] generateExcelLogFile(Map<String, String> errorLog);

    AssignResultVo assign(Long taskId);
    AssignResultVo assign(Long taskId, String calculateCircle  );

    void inheritPreviousRecordHandle(Long recoredId);

    String getReportRecordStatus(Long taskId);

    List<AssignResultVo> batchAssign(BatchAssignDto batchAssignDto);

}
