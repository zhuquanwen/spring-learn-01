package com.spring.learn.aop;

import com.spring.learn.aop.aspect.Advice;
import com.spring.learn.aop.config.AopConfig;
import com.spring.learn.aop.support.AdvisedSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 *
 * @author zhuquanwen
 * @vesion 1.0
 * @date 2021/10/5 14:27
 * @since jdk1.8
 */
public class JdkDynamicAopProxy implements InvocationHandler {
    private AdvisedSupport config;
    public JdkDynamicAopProxy(AdvisedSupport config) {
        this.config = config;
    }

    //动态生成一个代理类，由这个方法完成字节码重组
    public Object getProxy() {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), this.config.getTargetClass().getInterfaces(), this);
    }

    //完成代码织入的逻辑
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Map<String, Advice> advices = this.config.getAdvices(method, this.config.getTargetClass());
        invokeAdvice(advices.get("before"));

        //织入before的代码
        Object res = null;
        try {
            res = method.invoke(this.config.getTarget(), args);
        } catch (Exception e) {
            //织入afterThrow的代码
            invokeAdvice(advices.get("afterThrow"));
            throw e;
        }

        //织入after的代码
        invokeAdvice(advices.get("after"));

        return res;
    }

    private void invokeAdvice(Advice advice) throws InvocationTargetException, IllegalAccessException {
        advice.getAdviceMethod().invoke(advice.getAspect());
    }
}
