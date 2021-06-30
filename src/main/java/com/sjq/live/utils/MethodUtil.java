package com.sjq.live.utils;


import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;

public class MethodUtil {

    public static Method getDemoMethod() {
        Method[] methods = MethodUtil.class.getMethods();
        for (Method method : methods) {
            if (StringUtils.equals(method.getName(), "demo")) {
                return method;
            }
        }
        return null;
    }

    /**
     * demo方法，供内部使用
     * @return
     */
    public String demo() {
        return null;
    }
}
