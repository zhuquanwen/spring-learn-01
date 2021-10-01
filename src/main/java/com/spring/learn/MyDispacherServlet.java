package com.spring.learn;

import com.spring.learn.annotation.RequestParam;
import com.spring.learn.context.ApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author zhuquanwen
 * @vesion 1.0
 * @date 2020/11/29 21:01
 * @since jdk1.8
 */
public class MyDispacherServlet extends HttpServlet implements Constants {
    //初始化IoC
    private Properties contextConfig = new Properties();
    private Map<String, Method> reqMapping = new HashMap<>();
    private ApplicationContext context;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //6、分发请求
        doDispacher(req, resp);
    }

    private void doDispacher(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String requestURI = req.getRequestURI();
            String url = requestURI.replace(req.getContextPath(), "").replaceAll("/+", "/");
            if (!reqMapping.containsKey(url)) {
                resp.setStatus(404);
                resp.getWriter().println("404 Not Found");
                return;
            }
            Method method = reqMapping.get(url);
            Object[] args = new Object[method.getParameterCount()];
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes != null) {
                Map<String, String[]> parameterMap = req.getParameterMap();
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
            }

            Class<?> declaringClass = method.getDeclaringClass();
            Object invoke = method.invoke(this.context.getBean(declaringClass), args);
            resp.setStatus(200);
            resp.getWriter().println(invoke);

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            try {
                resp.getWriter().println("500 Server internal error");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        try {
            //初始化applicationContext
            context = new ApplicationContext(config.getInitParameter(INIT_PARAMETER_CONFIGURATION_LOCATION));



            //5、初始化HandlerMapping
//            doInitHandlerMapping();

            initStrategies(context);

            System.out.println("服务已启动");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initStrategies(ApplicationContext context) {
//        //多文件上传组件
//        initMultipartResolver(context);
//        //初始化本地语言环境
//        initLocaleResolver(context);
//        //初始化模板处理器
//        initThemeResolver(context);
        //handlerMapping
        initHandlerMappings(context);
        //初始化参数适配器
        initHandlerAdapters(context);
//        //初始化异常拦截器
//        initHandlerExceptionResolvers(context);
//        //初始化视图转换器
//        initRequestToViewNameTranslator(context);
        //初始化视图处理器
        initViewResolvers(context);
//        //flashMap管理器
//        initFlashMapManager(context);
    }

    private void initViewResolvers(ApplicationContext context) {

    }

    private void initHandlerAdapters(ApplicationContext context) {

    }

    private void initHandlerMappings(ApplicationContext context) {

    }

//    private void doInitHandlerMapping() throws Exception {
//        if (this.applicationContext.getBeanDefiniationCount() == 0) {
//            return;
//        }
//        String[] beanNames = applicationContext.getBeanDefiniationNames();
//        for (String beanName : beanNames) {
//            Object instance = applicationContext.getBean(beanName);
//            Class<?> aClass = instance.getClass();
//            if (!aClass.isAnnotationPresent(RestController.class)) {
//                continue;
//            }
//
//            //获取上层path
//            RequestMapping baseReqMapping = aClass.getAnnotation(RequestMapping.class);
//            String basePath = null;
//            if (baseReqMapping != null) {
//                basePath = baseReqMapping.value();
//            }
//
//            //只取public的函数
//            Method[] methods = aClass.getMethods();
//            for (Method method : methods) {
//                //函数必须被注解RequestMapping修饰
//                if (!method.isAnnotationPresent(RequestMapping.class)) {
//                    continue;
//                }
//                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
//                String url = requestMapping.value();
//                url = ("/" + basePath + "/" + url).replaceAll("/+", "/");
//                reqMapping.put(url, method);
//            }
//        }
//
//    }

}
