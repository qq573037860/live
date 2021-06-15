package com.sjq.live.utils;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by shenjq on 2019/12/2
 */
public class HttpUtils {

    /**
     * 去掉url中的路径，留下请求参数部分
     *
     * @param strURL url地址
     * @return url请求参数部分
     */
    private static String truncateUrlPath(String strURL) {
        strURL = strURL.trim().toLowerCase();
        String[] arrSplit = strURL.split("[?]");
        if (strURL.length() > 1 && arrSplit.length > 1 && Objects.nonNull(arrSplit[1])) {
            return arrSplit[1];
        }
        return null;
    }


    /**
     * 解析出url参数中的键值对
     * 如 "index.jsp?Action=del&id=123"，解析出Action:del,id:123存入map中
     *
     * @param strUrlParam url地址
     * @return url请求参数部分
     */
    public static Map<String, Object> decodeParamMap(String strUrlParam) {
        if (StringUtils.isEmpty(strUrlParam)) {
            return Collections.emptyMap();
        }

        Map<String, Object> mapRequest = Maps.newHashMap();
        String[] arrSplit;
        //每个键值为一组 www.2cto.com
        arrSplit = strUrlParam.split("[&]");
        for (String strSplit : arrSplit) {
            String[] arrSplitEqual = null;
            arrSplitEqual = strSplit.split("[=]");
            //解析出键值
            if (arrSplitEqual.length > 1) {
                //正确解析
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);
            } else {
                if (StringUtils.isNotEmpty(arrSplitEqual[0])) {
                    //只有参数没有值，不加入
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        return mapRequest;
    }


}
