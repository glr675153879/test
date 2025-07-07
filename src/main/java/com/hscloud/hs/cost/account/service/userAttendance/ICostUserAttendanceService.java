package com.hscloud.hs.cost.account.service.userAttendance;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.userAttendance.CostUserAttendanceDto;
import com.hscloud.hs.cost.account.model.dto.userAttendance.CostUserAttendanceEditDto;
import com.hscloud.hs.cost.account.model.dto.userAttendance.ExcelImportDTO;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.userAttendance.CostUserAttendance;
import com.hscloud.hs.cost.account.model.vo.userAttendance.ValidateJobNumberVO;
import com.hscloud.hs.cost.account.model.vo.userAttendance.export.ImportErrVo;
import com.pig4cloud.pigx.common.core.util.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 人员考勤表 服务接口类
 */
public interface ICostUserAttendanceService extends IService<CostUserAttendance> {

    void downloadTemplate(String dt, HttpServletResponse response);

    R handleFileUpload(MultipartFile file);

    ImportErrVo uploadFile(String[][] xlsDataArr, ExcelImportDTO dto, String dt);

    List<CostUserAttendanceDto> toMatchList(QueryWrapper<CostUserAttendance> wrapper);

    Page<CostUserAttendanceDto> manualMatchList(PageRequest<CostUserAttendance> pr);

    Page<CostUserAttendanceDto> sysMatchList(PageRequest<CostUserAttendance> pr);

    List<String> historyList(QueryWrapper<CostUserAttendance> wrapper);

    Boolean editWithCustomFields(CostUserAttendanceEditDto dto);

    Boolean updateData(CostUserAttendanceEditDto dto);

    Boolean lockData(String dt);

    IPage<CostUserAttendance> pageData(Page<CostUserAttendance> page, QueryWrapper<CostUserAttendance> wrapper);

    void importData(String dt) throws IOException;

    R validateData(String dt);

    List<ValidateJobNumberVO> validateJobNumber(String dt);

    void exportData(PageRequest<CostUserAttendance> pr, HttpServletResponse response);

    void calculateData(String s);

    // 将考勤表的自定义字段解析并放入一张单独的表格
    void pullCustomFields(String dt);

    void downloadError(String dt, HttpServletResponse response);

}
