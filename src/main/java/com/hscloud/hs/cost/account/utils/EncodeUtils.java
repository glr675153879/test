package com.hscloud.hs.cost.account.utils;

import cn.hutool.crypto.SmUtil;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * @Author: liangjie
 * @Date: 2020/9/27 0027 9:19
 */
@Slf4j
public class EncodeUtils {

    /**
     * SHA256
     *
     * @param str
     * @return
     */
    public static String SHA256(String str) {
        MessageDigest messageDigest;
        String encodeStr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes("UTF-8"));
            encodeStr = byte2Hex(messageDigest.digest());
        } catch (Exception e) {
            log.error("SHA256加密异常", e);
            throw new BizException("SHA256加密异常");
        }
        return encodeStr;
    }

    private static String byte2Hex(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        String temp;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
                // 1得到一位的进行补0操作
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

    /**
     * MD5
     *
     * @param str
     * @return
     */
    public static String MD5(String str) {
        //DigestUtils.md5Hex()
        return DigestUtils.md5Hex(str);
        //return DigestUtils.md5DigestAsHex(str.getBytes());
    }


    /**
     * SM3
     * @param str
     * @return
     */
    public static String SM3(String str){
        return SmUtil.sm3(str);
    }



}
