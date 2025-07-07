package com.hscloud.hs.cost.account.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hscloud.hs.cost.account.constant.enums.SignEncryptType;
import com.hscloud.hs.cost.account.model.dto.CostDataCollectionDto;
import com.hscloud.hs.cost.account.model.dto.GatewayApiDto;
import com.hscloud.hs.cost.account.model.entity.DataCollectionUrl;
import com.hscloud.hs.cost.account.model.pojo.ResponseData;
import com.hscloud.hs.cost.account.service.ICostDataCollectionService;
import com.hscloud.hs.cost.account.utils.GatewayApiClient;
import com.pig4cloud.pigx.common.core.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author banana
 * @create 2023-09-20 14:14
 */
@Service
public class CostDataCollectionServiceImpl implements ICostDataCollectionService {

    @Autowired
    private DataCollectionUrlServiceImpl dataCollectionUrlService;

    @Override
    public Object getDataByAppName(CostDataCollectionDto input) {
        //获取code对应的访问数据采集中心的基础入参详情（url、应用key、应用密钥、API名称、加密类型）
        DataCollectionUrl one = dataCollectionUrlService.getOne(new LambdaQueryWrapper<DataCollectionUrl>()
                .eq(DataCollectionUrl::getCode, input.getCode()));
        if(one == null)throw new BizException("当前code对应的数据内容不存在");

        //入参处理：
        //对入参中的dt字段进行处理 dt -> start_time ~ end_time
        JSONObject parameter = JSON.parseObject(JSON.toJSONString(input.getParameter()));
        if(parameter.get("dt") != null){
            String time = (String) parameter.get("dt");
            parameter.put("start_time", time.substring(0, time.indexOf('~')).trim());
            parameter.put("end_time", time.substring(time.indexOf("~") + 1).trim());
            parameter.remove("dt");
        }
        if(parameter.get("dt2") != null){
            String time = (String) parameter.get("dt2");
            parameter.put("start_time2", time.substring(0, time.indexOf('~')).trim());
            parameter.put("end_time2", time.substring(time.indexOf("~") + 1).trim());
            parameter.remove("dt2");
        }


        GatewayApiDto gatewayApiDto = new GatewayApiDto();
        //url
        gatewayApiDto.setUrl(one.getUrl());
        //应用密钥
        gatewayApiDto.setAppSecret(one.getAppSecret());
        //应用key
        gatewayApiDto.setAppKey(one.getAppKey());
        //API名称
        gatewayApiDto.setHeaderMethodType(one.getAppName());
        //加密类型
        if(one.getSignEncryptType().equals(SignEncryptType.SHA256.getCode()))
            gatewayApiDto.setSignEncryptType(SignEncryptType.SHA256);
        else if(one.getSignEncryptType().equals(SignEncryptType.MD5.getCode()))
            gatewayApiDto.setSignEncryptType(SignEncryptType.MD5);
        else if(one.getSignEncryptType().equals(SignEncryptType.SM3.getCode()))
            gatewayApiDto.setSignEncryptType(SignEncryptType.SM3);
        else throw new BizException("当前加密类型不合法!");

        //封装请求参数
        GatewayApiClient gatewayApiClient = new GatewayApiClient(gatewayApiDto);
        //获取数据采集中心的数据
        ResponseData responseData = gatewayApiClient.doPost(parameter);

        //出参处理：
        //字典大类和小类处理
        if("DICTCODE".equals(input.getCode()) && parameter.get("type") != null){
            //这里返回数据不能分页（字典接口一定不能分页），不然会报错！
            List<JSONObject> lists = (List<JSONObject>) responseData.getData();
            if("1".equals(parameter.get("type"))){
                //只要大类信息
                lists = lists.stream().peek(r -> {
                    r.remove("type2_code");
                    r.remove("type2_name");
                }).distinct().collect(Collectors.toList());
            }
            else if("2".equals(parameter.get("type"))){
                //只要小类信息
                lists.stream().forEach(r ->{
                    r.remove("type1_code");
                    r.remove("type1_name");
                });
            }
            responseData.setData(lists);
        }

        return responseData.getData();
    }
}
