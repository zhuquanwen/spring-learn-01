package com.spring.learn.test.aop;

/**
 *
 * @author zhuquanwen
 * @vesion 1.0
 * @date 2021/10/5 16:06
 * @since jdk1.8
 */
public class LogAspect {

    public void before() {
        System.out.println("before");
    }

    public void after() {
        System.out.println("after");
    }

    public void afterThrowing() {
        System.out.println("出现异常:");
    }
}
