package com.hscloud.hs.cost.account.controller.second;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.constant.CacheConstants;
import com.hscloud.hs.cost.account.model.dto.second.ProgrammePublishDTO;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.second.Programme;
import com.hscloud.hs.cost.account.model.vo.second.ProgrammeInfoVo;
import com.hscloud.hs.cost.account.service.second.IProgrammeService;
import com.hscloud.hs.cost.account.utils.RedisUtil;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import com.pig4cloud.pigx.common.security.service.PigxUser;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
* 方案
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/second/programme")
@Tag(name = "programme", description = "方案")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ProgrammeController {

    private final IProgrammeService programmeService;

    private final RedisUtil redisUtil;

    @SysLog("方案info")
    @GetMapping("/info/{id}")
    @Operation(summary = "方案info")
    public R<ProgrammeInfoVo> info(@PathVariable Long id) {
        return R.ok(programmeService.getProgrammeInfo(id));
    }

    @SysLog("方案page")
    @GetMapping("/page")
    @Operation(summary = "方案page")
    public R<IPage<Programme>> page(PageRequest<Programme> pr) {
        return R.ok(programmeService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("方案list")
    @GetMapping("/list")
    @Operation(summary = "方案list")
    public R<List<Programme>> list(PageRequest<Programme> pr) {
        return R.ok(programmeService.list(pr.getWrapper()));
    }

    @SysLog("方案add")
    @PostMapping("/add")
    @Operation(summary = "方案add")
    public R add(@RequestBody Programme programme)  {
        programme.setIfCommon("1");
        programme.setStatus("0");
        //判断是否 同个发放单元有 多个启用的方案
        programmeService.validGrantUnit(programme);

        programmeService.save(programme);
        return R.ok(programme);
    }

    @SysLog("方案copy")
    @PostMapping("/copy")
    @Operation(summary = "方案copy")
    public R copy(Long programmeId)  {
        programmeService.copy(programmeId);
        return R.ok();
    }

    //@SysLog("方案发布publish")
    @PostMapping("/publish")
    @Operation(summary = "方案发布publish")
    public R publish(@RequestBody ProgrammePublishDTO programmePublishDTO)  {
        String key = CacheConstants.SYNC_BY_PROGRAMME+programmePublishDTO.getProgrammeId();
        try{
            if(redisUtil.get(key) != null){
                return R.failed("请勿重复操作");
            }else{
                redisUtil.setLock(key,1,30L, TimeUnit.SECONDS);
            }
            programmeService.publish(programmePublishDTO);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            redisUtil.unLock(key);
        }
        return R.ok();
    }

    @SysLog("方案edit")
    @PostMapping("/edit")
    @Operation(summary = "方案edit")
    public R edit(@RequestBody Programme programme)  {
        PigxUser pigxUser = SecurityUtils.getUser();
        programme.setUpdateJobNumber(pigxUser.getJobNumber());
        programme.setUpdateBy(pigxUser.getName());
        //是否开启方案
        Programme programmeDB = programmeService.getById(programme.getId());
        if("1".equals(programmeDB.getStatus()) && "0".equals(programme.getStatus()) ){//开启
            //判断是否 同个发放单元有 多个启用的方案
            programmeService.validGrantUnit(programmeDB);
            programmeService.startStatus(programme,"0");
            return R.ok();
        }else  if("0".equals(programmeDB.getStatus()) && "1".equals(programme.getStatus()) ){//禁用
            programmeService.startStatus(programme,"1");
            return R.ok();
        }else{
            programmeService.validGrantUnit(programme);
        }

        return R.ok(programmeService.updateById(programme));
    }

    @SysLog("方案del")
    @PostMapping("/del/{id}")
    @Operation(summary = "方案del")
    public R del(@PathVariable Long id)  {
        programmeService.deleteById(id);
        return R.ok();
    }

    @SysLog("方案delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "方案delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(programmeService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}