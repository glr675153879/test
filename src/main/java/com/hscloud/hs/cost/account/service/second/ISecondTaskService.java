package com.hscloud.hs.cost.account.service.second;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.second.SecondTaskCreateDto;
import com.hscloud.hs.cost.account.model.entity.second.SecondTask;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
/**
* 二次分配总任务 服务接口类
*/
public interface ISecondTaskService extends IService<SecondTask> {

    /**
     * 根据一次任务id 下发二次任务
     * @param taskCreateDto
     */
    void create(SecondTaskCreateDto taskCreateDto);

    /**
     * 检查是否所有unitTask都完成
     * @param secondTaskId
     */
    void finishCheck(Long secondTaskId);

    /**
     * 检查同周期的二次分配任务 是否存在
     * @param firstId
     * @return
     */
    Boolean ifPublished(Long firstId);

    IPage<SecondTask> taskPage(PageRequest<SecondTask> pr);
}
