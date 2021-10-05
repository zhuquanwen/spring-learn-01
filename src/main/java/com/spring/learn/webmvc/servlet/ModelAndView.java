package com.spring.learn.webmvc.servlet;

import java.util.Map;

/**
 *
 * @author zhuquanwen
 * @vesion 1.0
 * @date 2021/10/5 11:06
 * @since jdk1.8
 */
public class ModelAndView {
    private String viewName;
    private Map<String, ?> model;

    public ModelAndView(String viewName) {
        this.viewName = viewName;

    }

    public ModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }

    public void setModel(Map<String, ?> model) {
        this.model = model;
    }
}
