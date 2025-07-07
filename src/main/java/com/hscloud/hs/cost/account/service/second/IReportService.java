package com.hscloud.hs.cost.account.service.second;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.MingBeiExcel;
import com.hscloud.hs.cost.account.model.dto.second.RepotHulijxValueDTO;
import com.hscloud.hs.cost.account.model.dto.second.RepotZhigongjxValueDTO;
import com.hscloud.hs.cost.account.model.dto.second.RepotZhigongjxflValueDTO;
import com.hscloud.hs.cost.account.model.vo.report.SumZhigongjxVO;
import com.hscloud.hs.cost.account.model.vo.report.SumZhigongjxflVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author 小小w
 * @date 2024/3/9 11:46
 */
public interface IReportService {


    Page<RepotZhigongjxValueDTO> zhigongjxList(String cycle, String endCycle, String userName, String isMingBei, Page page);

    List<RepotHulijxValueDTO> hulijxList(String cycle);

    void exportZhigongjx(String cycle, HttpServletResponse response);

    void exportHulijx(String cycle, HttpServletResponse response);

    List<RepotZhigongjxValueDTO> exportZhigongjx(String cycle, String endCycle, String userName);

    /**
     * 获取最新周期
     *
     * @return
     */
    String lastCycle();

    Page<RepotZhigongjxflValueDTO> zhigongjxflList(String cycle, String endCycle, String userName, String isMingBei, Page page);

    List<RepotZhigongjxflValueDTO> exportZhigongjxfl(String cycle, String endCycle, String userName);

    List<MingBeiExcel> exportMingBei(String cycle, String endCycle, String userName);

    SumZhigongjxVO sumZhigongjx(String cycle, String endCycle, String userName);

    SumZhigongjxflVO sumZhigongjxfl(String cycle, String endCycle,String userName, String isMingBei);
}
