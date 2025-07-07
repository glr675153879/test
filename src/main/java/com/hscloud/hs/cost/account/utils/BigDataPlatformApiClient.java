package com.hscloud.hs.cost.account.utils;

import cn.hutool.core.lang.Pair;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hscloud.hs.cost.account.constant.enums.JTBigDataPlatformApiTypeEnum;
import com.hscloud.hs.cost.account.model.entity.OdsDisKslyxx;
import com.pig4cloud.pigx.admin.api.feign.RemoteParamService;
import com.pig4cloud.pigx.common.core.constant.SecurityConstants;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 金唐大数据平台适用api客户端
 * @author banana
 * @create 2023-10-19 9:55
 */
@Slf4j
@Component
@Data
public class BigDataPlatformApiClient implements InitializingBean {

    @Value("#{${params.employee}}")
    private HashMap<String, String> employee = new HashMap<>();

    @Value("#{${params.dept}}")
    private HashMap<String, String> dept = new HashMap<>();

    @Value("#{${params.tracing-item}}")
    private HashMap<String, String> tracingItem = new HashMap<>();

    @Value("${jt.bigdata.call.timeout:60000}")
    private Integer timeout;

    @Autowired
    private RemoteParamService remoteParamService;

    @Autowired
    private SqlUtil sqlUtil;

    private HashMap<Integer, HashMap<String, String>> paramsMap = new HashMap<>();

    private HashMap<Integer, String> regexMap = new HashMap<>();

    private HashMap<Integer, String> requestBodyMap = new HashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        paramsMap.put(JTBigDataPlatformApiTypeEnum.EMPLOYEE.getType(), employee);
        paramsMap.put(JTBigDataPlatformApiTypeEnum.DEPT.getType(), dept);
        paramsMap.put(JTBigDataPlatformApiTypeEnum.TRACINGITEMS.getType(), tracingItem);

        regexMap.put(JTBigDataPlatformApiTypeEnum.EMPLOYEE.getType(), "\\{\"EMPLOYEE\":(.*)}");
        regexMap.put(JTBigDataPlatformApiTypeEnum.DEPT.getType(), "\\{\"SERV_HSB_CDR_DEPT\":(.*)}");
        regexMap.put(JTBigDataPlatformApiTypeEnum.TRACINGITEMS.getType(), "<!\\[CDATA\\[(.*?)\\]\\]>");

        //TODO 通用请求的优化处理
        requestBodyMap.put(JTBigDataPlatformApiTypeEnum.EMPLOYEE.getType(), "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:won=\"www.WondersHSBP.com\">\n" +
                "   <soap:Header/>\n" +
                "   <soap:Body>\n" +
                "      <won:invokeRequest>\n" +
                "         <won:parameter>\n" +
                "         <![CDATA[\n" +
                "        <body><head><userid>#{userid}</userid><password>#{password}</password><trans_no>#{transNo}</trans_no>\n" +
                "        </head><request><BRANCH_CODE>#{branchCode}</BRANCH_CODE><BEGIN_TIME>#{beginTime}</BEGIN_TIME><END_TIME>#{endTime}</END_TIME></request></body>\n" +
                "         ]]>\n" +
                "         </won:parameter>\n" +
                "      </won:invokeRequest>\n" +
                "   </soap:Body>\n" +
                "</soap:Envelope>");
        requestBodyMap.put(JTBigDataPlatformApiTypeEnum.DEPT.getType(), "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:won=\"www.WondersHSBP.com\">\n" +
                "   <soap:Header/>\n" +
                "   <soap:Body>\n" +
                "      <won:invokeRequest>\n" +
                "         <won:parameter>\n" +
                "         <![CDATA[\n" +
                "        <body><head><userid>#{userid}</userid><password>#{password}</password><trans_no>#{transNo}</trans_no>\n" +
                "        </head><request><BRANCH_CODE>#{branchCode}</BRANCH_CODE><BEGIN_TIME>#{beginTime}</BEGIN_TIME><END_TIME>#{endTime}</END_TIME></request></body>\n" +
                "         ]]>\n" +
                "         </won:parameter>\n" +
                "      </won:invokeRequest>\n" +
                "   </soap:Body>\n" +
                "</soap:Envelope>");
        requestBodyMap.put(JTBigDataPlatformApiTypeEnum.TRACINGITEMS.getType(), "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:won=\"www.WondersHSBP.com\">\n" +
                "   <soap:Header/>\n" +
                "   <soap:Body>\n" +
                "      <won:invokeRequest>\n" +
                "         <won:parameter>\n" +
                "         <![CDATA[\n" +
                "<body><head><userid>#{userid}</userid><password>#{password}</password><trans_no>#{transNo}</trans_no></head><request><DEPT_CODE>#{deptCode}</DEPT_CODE><START_DATE>#{startDate}</START_DATE><END_DATE>#{endDate}</END_DATE></request></body>\n" +
                "         ]]>\n" +
                "         </won:parameter>\n" +
                "      </won:invokeRequest>\n" +
                "   </soap:Body>\n" +
                "</soap:Envelope>");
    }

    /**
     * 调用接口请求方法
     * @param url 请求url
     * @param type 通用请求类型
     * @return 处理后的响应结果
     */
    private String commonCall(String url, Integer type){
        //请求入参处理
        String originalRequestBody = requestBodyMap.get(type);
        HashMap<String, String> param = paramsMap.get(type);
        String newRequestBody = replaceRequestParams(originalRequestBody, param);
        log.info("通用请求信息：{}", requestBodyMap.get(type));
        log.info("通用请求入参：{}", param);
        log.info("发起请求体信息：{}", newRequestBody);
        //封装请求数据
        HttpRequest request = HttpRequest.post(url)
                .header(Header.CONTENT_TYPE, " text/xml;charset=utf-8")
                .timeout(timeout)
                .body(newRequestBody);

        //调用
        HttpResponse executeResult = null;
        try {
            executeResult = request.execute();
        } catch (Exception e) {
            log.error("发起请求出现异常:{}", e.getMessage());
            throw new BizException("发起请求出现异常");
        }
        log.info("请求成功");

        //出参处理
        String originalResponseBody = executeResult.body();
        log.info("原始响应体信息：{}", originalResponseBody);
        String responseBodyData = getResponseBodyData(originalResponseBody, type);
        log.info("提取的响应体信息：{}", responseBodyData);
        String res = analysisResponseBodyData(responseBodyData,type);
        log.info("封装后的数据信息:{}", res);
        return res;
    }

    /**
     * 对请求的入参进行处理
     * @param requestBody 请求body信息（统一使用#{xxx}替代入参信息）
     * @param params 入参信息
     * @return 替换#{xxx}后的请求body
     */
    private String replaceRequestParams(String requestBody, Map<String, String> params){
        log.info("原请求体信息：{}", requestBody);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String paramName = entry.getKey();
            String paramValue = entry.getValue();
            String paramPlaceholder = "#{" + paramName + "}";
            if (!requestBody.contains(paramPlaceholder)) {
                continue;
            }
            requestBody = requestBody.replace(paramPlaceholder, paramValue);

            paramPlaceholder = "${" + paramName + "}";
            requestBody = requestBody.replace(paramPlaceholder, paramValue);
        }
        requestBody = requestBody.replaceAll("\\s+", " ").trim();
        log.info("处理后请求体信息：{}", requestBody);
        return requestBody;
    }


    private String getResponseBodyData(String responseBody, Integer type){
        Pattern pattern = Pattern.compile(regexMap.get(type), Pattern.DOTALL);
        Matcher matcher = pattern.matcher(responseBody);

        String responseBodyData = null;
        if(matcher.find()){
            responseBodyData = matcher.group(1).trim();
        }else{
            throw new BizException("当前响应数据解析出现异常");
        }
        return responseBodyData;
    }

    private String analysisResponseBodyData(String responseBodyData, Integer type){
        String res = "analysis error";
        if(type.equals(JTBigDataPlatformApiTypeEnum.EMPLOYEE.getType()) ||
                type.equals(JTBigDataPlatformApiTypeEnum.DEPT.getType())){
            /**
             * 1、2 解析说明：
             * jsonstring --> JSONArray
             * JSONArray jsonArray = JSONUtil.parseArray(res);
             */
            JSONArray jsonArrays = JSONUtil.parseArray(responseBodyData);
            res = JSONUtil.toJsonStr(jsonArrays);

        }else if(type.equals(JTBigDataPlatformApiTypeEnum.TRACINGITEMS.getType())){
            /**
             * 3 解析说明
             * JSONArray --> String
             * JSONUtil.toJsonStr(jsonArray);
             *
             * String --> JSONARRAY
             * JSONUtil.parseArray(String)
             */

            SAXReader reader = new SAXReader();
            Document document = null;
            try {
                document = reader.read(new StringReader(responseBodyData));
            } catch (DocumentException e) {

            }

            Element rootElement = document.getRootElement();
            Element responseElement = rootElement.element("response"); // 获取'response'元素

            //响应的结果是否成功，即ret_code是否为0
            String code = responseElement.elementText("ret_code");
            log.info("code:{}", code);
            if(code.equals("1")){
                throw new BizException("调用接口发生异常！");
            }

            //响应的结果信息
            String msg = responseElement.elementText("ret_info");
            log.info("msg:{}", msg);

            //封装返回的date信息
            JSONArray datas = new JSONArray();
            List<Element> elements = responseElement.elements("Info");
            for(Element element : elements){
                OdsDisKslyxx data = new OdsDisKslyxx();
                //时间粒度为日
                String beginTime = paramsMap.get(type).get("startDate");
                if(StringUtils.isNotBlank(beginTime)){
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime dateTime = LocalDateTime.parse(beginTime, formatter);
                    DateTimeFormatter targetFormat  = DateTimeFormatter.ofPattern("yyyyMMdd");
                    String date = dateTime.format(targetFormat );
                    data.setDt(date);
                }
                data.setSeq("1");
                data.setDeptCode(element.elementText("DEPT_CODE"));
                data.setDeptName(element.elementText("DEPT_NAME"));
                data.setDisposableNum(new BigDecimal(element.elementText("DISPOSABLE_NUM")));
                data.setNonDisposableNum(new BigDecimal(element.elementText("NON_DISPOSABLE_NUM")));
                data.setDisposableAmount(new BigDecimal(element.elementText("DISPOSABLE_AMOUNT")));
                data.setNonDisposableAmount(new BigDecimal(element.elementText("NON_DISPOSABLE_AMOUNT")));
                data.setNum(new BigDecimal(element.elementText("NUM")));
                data.setAmount(new BigDecimal(element.elementText("AMOUNT")));

                JSONObject jsonObject = JSONUtil.parseObj(data);
                datas.add(jsonObject);
            }
            res = JSONUtil.toJsonStr(datas);
        }
        return res;
    }


    /**
     * 入参日期内容替换
     * @param type 当前接口类型
     * @param startDate 开始时间
     * @param endDate 结束时间
     */
    public void replaceInputParamDate(Integer type, String startDate, String endDate){
        //获取当前类型的入参信息
        HashMap<String, String> params = paramsMap.get(type);
        //替换日期信息
        if(JTBigDataPlatformApiTypeEnum.EMPLOYEE.getType().equals(type) ||
                JTBigDataPlatformApiTypeEnum.DEPT.getType().equals(type)){
            //暂缺，一般调用这两个类型属于同步组织架构，一般不传时间
        }
        else if(JTBigDataPlatformApiTypeEnum.TRACINGITEMS.getType().equals(type)){
            params.replace("startDate", startDate);
            params.replace("endDate", endDate);
        }
        //更新
        paramsMap.replace(type, params);
        log.info("实际入参信息:{}", params);
    }

    /**
     * 调取接口获取粒度为日的信息
     */
    public String callInterface(Integer type){
        //1.调用接口获取数据
        String typeUrl = JTBigDataPlatformApiTypeEnum.getTypeUrl(type);
        if(StringUtils.isBlank(typeUrl)){
            log.error("根据type获取的url不存在");
            throw new BizException("根据type获取的url不存在");
        }
        String url = remoteParamService.getByKey(typeUrl, SecurityConstants.FROM_IN).getData();
        log.info("请求url：{}", url);
        return commonCall(url, type);
    }

    public void save(Integer type, String jsonStr){
        if(JTBigDataPlatformApiTypeEnum.TRACINGITEMS.getType().equals(type)){
            JSONArray datas = JSONUtil.parseArray(jsonStr);
            //获得已经获取过的信息
            HashMap<Pair<String, String>, Integer> hashMap = new HashMap<>();
            List<OdsDisKslyxx> lists = sqlUtil.getOdsDisKslyxx();
            for(OdsDisKslyxx data : lists){
                if(data.getDt() == null || data.getDeptCode() == null)continue;
                Pair<String, String> pair = new Pair<>(data.getDt(), data.getDeptCode());
                if(hashMap.get(pair) == null){
                    hashMap.put(pair, 1);
                }else{
                    hashMap.replace(pair, Math.max(hashMap.get(pair), Integer.valueOf(data.getSeq())));
                }
            }

            for(Object data : datas) {
                JSONObject jsonObject = (JSONObject) data;
                OdsDisKslyxx odsDisKslyxx = new OdsDisKslyxx();
                /**
                 * 逻辑处理
                 * 1. dt的时间粒度是按日
                 * 2. seq是数据接受序号，同日期同科室接受多次的，seq自增
                 */
                odsDisKslyxx.setDt(jsonObject.getStr("dt"));
                odsDisKslyxx.setAmount(new BigDecimal(jsonObject.getStr("amount")));
                odsDisKslyxx.setDeptCode(jsonObject.getStr("deptCode"));
                odsDisKslyxx.setDeptName(jsonObject.getStr("deptName"));
                odsDisKslyxx.setDisposableAmount(new BigDecimal(jsonObject.getStr("disposableAmount")));
                odsDisKslyxx.setDisposableNum(new BigDecimal(jsonObject.getStr("disposableNum")));
                odsDisKslyxx.setNonDisposableAmount(new BigDecimal(jsonObject.getStr("nonDisposableAmount")));
                odsDisKslyxx.setNonDisposableNum(new BigDecimal(jsonObject.getStr("nonDisposableNum")));
                odsDisKslyxx.setNum(new BigDecimal(jsonObject.getStr("num")));
                odsDisKslyxx.setCreateDate(LocalDateTime.now());

                //当前容器和科室
                Pair<String, String> pair = new Pair<>(odsDisKslyxx.getDt(), odsDisKslyxx.getDeptCode());
                if(hashMap.get(pair) == null){
                    hashMap.put(pair, 1);
                }else{
                    hashMap.replace(pair, hashMap.get(pair) + 1);
                }
                odsDisKslyxx.setSeq(hashMap.get(pair).toString());

                //通过jdbcTemplate去执行插入操作
                try {
                    sqlUtil.insertOdsDisKslyxx(odsDisKslyxx);
                } catch (Exception e) {
                    log.error("执行插入数据表操作时发生错误，错误信息：{}", e.getMessage());
                    throw new BizException("执行插入数据表操作时发生错误");
                }
            }
        }
        else if(JTBigDataPlatformApiTypeEnum.EMPLOYEE.getType().equals(type) ||
                JTBigDataPlatformApiTypeEnum.DEPT.getType().equals(type)){
            //暂定
        }
    }
}
