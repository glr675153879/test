package com.hscloud.hs.cost.account.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.hscloud.hs.cost.account.model.dto.GatewayApiDto;
import com.hscloud.hs.cost.account.model.pojo.RequestHeader;
import com.hscloud.hs.cost.account.model.pojo.ResponseData;
import com.pig4cloud.pigx.common.core.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * 数据采集平台调用接口工具类
 */
public class GatewayApiClient {
    //日志打印
    private static final Logger logger = LoggerFactory.getLogger(GatewayApiClient.class);

    private GatewayApiDto gatewayApiDto;
    private String timeStamp;

    public GatewayApiClient(GatewayApiDto gatewayApiDto) {
        this.gatewayApiDto = gatewayApiDto;
        this.timeStamp = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
    }

    public ResponseData doPost(JSONObject requestBody) {
        //参数校验
        this.requestValid();
        //生成header
        RequestHeader requestHeader = this.createRequestHeader();
        //发送请求
		 String response = HttpRequest.post(this.gatewayApiDto.getUrl())
                .headerMap(requestHeader.getParamsMap(), false)
                .timeout(20000)
                .body(String.valueOf(requestBody))
                .execute().body();
        return this.getResponseData(response);
    }

    private ResponseData getResponseData(String response) {
        ResponseData responseData;
        if (StringUtils.isEmpty(response)) {
            throw new BizException("can't connect to " + this.gatewayApiDto.getUrl());
        }
        try {
            responseData = JSONObject.parseObject(response, ResponseData.class);
        } catch (Exception e) {
            logger.error("e:" + e.getMessage());
            logger.error("illegal response format : {}", response);
            throw new BizException("illegal response format");
        }
        if (!responseData.getSuccess() || responseData.getCode() != 0) {
            logger.error("failed response : {}", response);
            throw new BizException("failed response msg : " + responseData.getMsg());
        }
        return responseData;
    }


    private void requestValid() {
        if (this.gatewayApiDto == null) {
            throw new BizException("apiConfig can't be null !");
        }
        if (StringUtils.isEmpty(this.gatewayApiDto.getUrl())) {
            throw new BizException("url can't be null !");
        }
        if (StringUtils.isEmpty(this.gatewayApiDto.getAppKey())) {
            throw new BizException("appKey can't be null !");
        }
        if (StringUtils.isEmpty(this.gatewayApiDto.getAppSecret())) {
            throw new BizException("appSecret can't be null !");
        }
        if (this.gatewayApiDto.getSignEncryptType() == null) {
            throw new BizException("encryptType can't be null !");
        }
    }

    private RequestHeader createRequestHeader() {
        RequestHeader requestHeader = new RequestHeader();
        requestHeader.setAppKey(this.gatewayApiDto.getAppKey());
        requestHeader.setTimestamp(this.timeStamp);
        requestHeader.setMethod(this.gatewayApiDto.getHeaderMethodType());
        requestHeader.setSign(createSign());
        return requestHeader;
    }

    private String createSign() {
        String beforeEncryptStr = this.gatewayApiDto.getAppKey() +
                this.gatewayApiDto.getAppSecret() +
                this.gatewayApiDto.getHeaderMethodType() +
                this.timeStamp;

        switch (this.gatewayApiDto.getSignEncryptType().getCode()) {
            case "MD5":
                return EncodeUtils.MD5(beforeEncryptStr);
            case "SHA256":
                return EncodeUtils.SHA256(beforeEncryptStr);
            case "SM3":
                return EncodeUtils.SM3(beforeEncryptStr);
            default:
                throw new BizException("illegal signEncryptType");
        }

    }
}
