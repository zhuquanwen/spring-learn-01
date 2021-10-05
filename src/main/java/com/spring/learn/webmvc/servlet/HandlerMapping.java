package com.spring.learn.webmvc.servlet;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 *
 * @author zhuquanwen
 * @vesion 1.0
 * @date 2021/10/5 10:10
 * @since jdk1.8
 */
public class HandlerMapping {
    private Pattern pattern;
    private Object controller;
    private Method method;

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public HandlerMapping() {
    }

    public HandlerMapping(Pattern pattern, Object controller, Method method) {
        this.pattern = pattern;
        this.controller = controller;
        this.method = method;
    }
}
