package com.spring.learn.aop.support;

import com.spring.learn.aop.aspect.Advice;
import com.spring.learn.aop.config.AopConfig;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author zhuquanwen
 * @vesion 1.0
 * @date 2021/10/5 14:26
 * @since jdk1.8
 */
public class AdvisedSupport {
    private Object target;
    private Class targetClass;
    private AopConfig aopConfig;
    private Pattern pointCutClassPattern;
    private Map<Method, Map<String, Advice>> methodCache = new HashMap<>();

    public AdvisedSupport(AopConfig aopConfig) {
        this.aopConfig = aopConfig;
    }

    public boolean pointCutMatch() {
        return pointCutClassPattern.matcher(this.targetClass.getName()).matches();
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
        parse();
    }

    public Class getTargetClass() {
        return targetClass;
    }

    private void parse() {
        String pointCutRegex = this.aopConfig.getPointCut().replaceAll("\\.", "\\\\.")
                .replaceAll("\\\\.\\*", ".*")
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)");
        String pointCutForClassRegex = pointCutRegex.substring(0, pointCutRegex.lastIndexOf("\\(") - 4);
        pointCutForClassRegex = pointCutForClassRegex.substring(pointCutForClassRegex.lastIndexOf(" ") + 1);
        this.pointCutClassPattern = Pattern.compile(pointCutForClassRegex);

        //将切面的方法提取出来，缓存起来
        Map<String, Method> aspectMethods = new HashMap<>();
        try {
            Class<?> aspectClass = Class.forName(this.aopConfig.getAspectClass());
            for (Method method : aspectClass.getMethods()) {
                aspectMethods.put(method.getName(), method);
            }

            //用正则匹配目标类的方法，如果匹配上就和切面方法建立关系
            Pattern pointCutPattern = Pattern.compile(pointCutRegex);
            for (Method method : this.targetClass.getMethods()) {
                String methodString = method.toString();
                if (methodString.contains("throws")) {
                   methodString = methodString.substring(0, methodString.lastIndexOf("throws")).trim();
                }
                Matcher matcher = pointCutPattern.matcher(methodString);
                if (matcher.matches()) {
                    Map<String, Advice> advices = new HashMap<>();
                    if (this.aopConfig.getAspectBefore() != null && !Objects.equals(this.aopConfig.getAspectBefore(), "")) {
                        advices.put("before", new Advice(aspectClass.newInstance(), aspectMethods.get(this.aopConfig.getAspectBefore())));
                    }
                    if (this.aopConfig.getAspectAfter() != null && !Objects.equals(this.aopConfig.getAspectAfter(), "")) {
                        advices.put("after", new Advice(aspectClass.newInstance(), aspectMethods.get(this.aopConfig.getAspectAfter())));
                    }
                    if (this.aopConfig.getAspectAfterThrow() != null && !Objects.equals(this.aopConfig.getAspectAfterThrow(), "")) {
                        Advice advice = new Advice(aspectClass.newInstance(), aspectMethods.get(this.aopConfig.getAspectAfterThrow()));
                        advice.setThrowName(this.aopConfig.getAspectAfterThrowingName());
                        advices.put("afterThrow", advice);
                    }
                    methodCache.put(method, advices);
                }
            }

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
    }

    public Map<String, Advice> getAdvices(Method method, Class targetClass) throws NoSuchMethodException {
        Map<String, Advice> cache = this.methodCache.get(method);
        if (cache == null) {
            Method m = targetClass.getMethod(method.getName(), method.getParameterTypes());
            cache = this.methodCache.get(m);
            this.methodCache.put(m, cache);
        }
        return cache;
    }
}
