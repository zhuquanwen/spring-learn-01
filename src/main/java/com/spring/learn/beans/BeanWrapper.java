package com.spring.learn.beans;

/**
 *
 * @author zhuquanwen
 * @vesion 1.0
 * @date 2021/9/27 20:33
 * @since jdk1.8
 */
public class BeanWrapper {

    private Object wrapperInstance;

    private Class<?> wrapperClass;


    public BeanWrapper(Object wrapperInstance) {
        this.wrapperInstance = wrapperInstance;
        this.wrapperClass = wrapperInstance.getClass();
    }

    public Object getWrapperInstance() {
        return wrapperInstance;
    }

    public void setWrapperInstance(Object wrapperInstance) {
        this.wrapperInstance = wrapperInstance;
    }

    public Class<?> getWrapperClass() {
        return wrapperClass;
    }

    public void setWrapperClass(Class<?> wrapperClass) {
        this.wrapperClass = wrapperClass;
    }
}
