package com.spring.learn.webmvc.servlet;

import com.spring.learn.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

/**
 * @author zhuquanwen
 * @vesion 1.0
 * @date 2021/10/5 10:29
 * @since jdk1.8
 */
public class HandlerAdaptor {
    public ModelAndView handle(HttpServletRequest req, HttpServletResponse resp, HandlerMapping handlerMapping) throws InvocationTargetException, IllegalAccessException {
        Map<String, String[]> parameterMap = req.getParameterMap();
        Method method = handlerMapping.getMethod();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] args = new Object[method.getParameterCount()];
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (parameterType == HttpServletRequest.class) {
                args[i] = req;
            } else if (parameterType == HttpServletResponse.class) {
                args[i] = resp;
            } else {
                Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                if (parameterAnnotations != null) {
                    for (int i1 = 0; i1 < parameterAnnotations.length; i1++) {
                        for (Annotation parameterAnnotation : parameterAnnotations[i1]) {
                            //暂时只支持RequestParam
                            if (parameterAnnotation instanceof RequestParam) {
                                String requestParamName = ((RequestParam) parameterAnnotation).value().trim();
                                if (!"".equals(requestParamName)) {
                                    String param = Arrays.toString(parameterMap.get(requestParamName))
                                            .replaceAll("\\[|\\]", "")
                                            .replaceAll("\\s", "");
                                    args[i] = param;
                                }
                            }
                        }
                    }
                }
            }
        }
        Object result = method.invoke(handlerMapping.getController(), args);
        if (result == null || result instanceof Void) {
            return null;
        }
        boolean isMv = handlerMapping.getMethod().getReturnType() == ModelAndView.class;
        if (isMv) {
            return (ModelAndView) result;
        }
        return null;
    }
}
