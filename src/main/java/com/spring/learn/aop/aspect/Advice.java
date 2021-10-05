package com.spring.learn.aop.aspect;

import java.lang.reflect.Method;

/**
 *
 * @author zhuquanwen
 * @vesion 1.0
 * @date 2021/10/5 14:27
 * @since jdk1.8
 */
public class Advice {
    private Object aspect;
    private Method adviceMethod;
    private String throwName;

    public Advice(Object aspect, Method adviceMethod) {
        this.aspect = aspect;
        this.adviceMethod = adviceMethod;
    }

    public Object getAspect() {
        return aspect;
    }

    public void setAspect(Object aspect) {
        this.aspect = aspect;
    }

    public Method getAdviceMethod() {
        return adviceMethod;
    }

    public void setAdviceMethod(Method adviceMethod) {
        this.adviceMethod = adviceMethod;
    }

    public String getThrowName() {
        return throwName;
    }

    public void setThrowName(String throwName) {
        this.throwName = throwName;
    }
}
