package com.spring.learn;

import com.spring.learn.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * @author zhuquanwen
 * @vesion 1.0
 * @date 2020/11/29 21:01
 * @since jdk1.8
 */
public class MyDispacherServlet extends HttpServlet {
    //初始化IoC
    private Map<String, Object> ioc = new HashMap<>();
    private Properties contextConfig = new Properties();
    private Set<Class<?>> classes = new HashSet<>();
    private Map<String, Method> reqMapping = new HashMap<>();

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
//            //暂时只支持RequestParam
//            Parameter[] parameters = method.getParameters();
//            if (parameters != null) {
//                for (int i = 0; i < parameters.length; i++) {
//                    if (parameters[i].isAnnotationPresent(RequestParam.class)) {
//                        RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);
//                        String parameter = req.getParameter(requestParam.value());
//                        //需要做类型转换，暂时认为参数只能是String
//                        args[i] = parameter;
//                    }
//                }
//            }
            Class<?> declaringClass = method.getDeclaringClass();
            String simpleName = declaringClass.getSimpleName();
            simpleName = firstLower(simpleName);
            Object invoke = method.invoke(ioc.get(simpleName), args);
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
            //1、加载配置文件
            doLoadConfig(config);

            //2、扫描相关的类
            doScanner(contextConfig.getProperty("scanPackage"));

            //3、实例化扫描到的类，并且缓存到IoC容器中
            doInstance();

            //4、完成依赖注入
            doAutowired();

            //5、初始化HandlerMapping
            doInitHandlerMapping();

            System.out.println("服务已启动");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doInitHandlerMapping() {
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> aClass = entry.getValue().getClass();
            if (!aClass.isAnnotationPresent(Controller.class)) {
                continue;
            }

            //获取上层path
            RequestMapping baseReqMapping = aClass.getAnnotation(RequestMapping.class);
            String basePath = null;
            if (baseReqMapping != null) {
                basePath = baseReqMapping.value();
            }

            //只取public的函数
            Method[] methods = aClass.getMethods();
            for (Method method : methods) {
                //函数必须被注解RequestMapping修饰
                if (!method.isAnnotationPresent(RequestMapping.class)) {
                    continue;
                }
                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                String url = requestMapping.value();
                url = ("/" + basePath + "/" + url).replaceAll("/+", "/");
                reqMapping.put(url, method);
            }
        }
    }

    private void doAutowired() throws IllegalAccessException {
        Set<Object> inited = new HashSet<>();
        for (Object o : ioc.values()) {
            if (inited.contains(o)) {
                continue;
            }
            Field[] declaredFields = o.getClass().getDeclaredFields();
            if (declaredFields != null) {
                for (Field declaredField : declaredFields) {
                    if (!declaredField.isAnnotationPresent(Autowired.class)) {
                        continue;
                    }
                    Autowired autowired = declaredField.getAnnotation(Autowired.class);
                    String beanName = autowired.name();
                    if ("".equals(beanName)) {
                        String name = declaredField.getType().getSimpleName();
                        beanName = firstLower(name);
                    }
                    Object fieldObj = ioc.get(beanName);
                    declaredField.setAccessible(true);
                    declaredField.set(o, fieldObj);

                }
            }
            inited.add(o);
        }
    }

    private void doInstance() throws Exception {

        for (Class<?> aClass : classes) {

            //如果没有Component等注解，跳过
            if (!aClass.isAnnotationPresent(Component.class) &&
                    !aClass.isAnnotationPresent(Controller.class) &&
                    !aClass.isAnnotationPresent(Service.class) &&
                    !aClass.isAnnotationPresent(Repository.class)) {
                continue;
            }

            //如果是接口，跳过
            if (aClass.isInterface()) {
                continue;
            }

            String beanName = null;
            //取到Component的value作为Bean的name
            Component component = aClass.getAnnotation(Component.class);
            String value = component != null ? component.value() : null;
            value = value != null ? value : aClass.getAnnotation(Service.class) != null ? aClass.getAnnotation(Service.class).value() : null;
            value = value != null ? value : aClass.getAnnotation(Controller.class) != null ? aClass.getAnnotation(Controller.class).value() : null;
            value = value != null ? value : aClass.getAnnotation(Repository.class) != null ? aClass.getAnnotation(Repository.class).value() : null;


            if (!"".equals(value)) {
                beanName = value;
            } else {
                //默认类名首字母小写
                beanName = firstLower(aClass.getSimpleName());
            }

            if (ioc.containsKey(beanName)) {
                //如果有重名的key,报错
                throw new Exception(String.format("有两个重复的BeanName[%s],分别是[%s],[%s]",
                        beanName, ioc.get(beanName).getClass().getName(), aClass.getName()));
            }

            Object o = aClass.getDeclaredConstructor().newInstance();
            ioc.put(beanName, o);

            Class<?>[] interfaces = aClass.getInterfaces();
            if (interfaces != null) {
                for (Class<?> anInterface : interfaces) {
                    String interfaceBeanName = firstLower(anInterface.getSimpleName());
                    if (ioc.containsKey(interfaceBeanName) && "".equals(value)) {
                        //如果有重名的key,报错
                        throw new Exception(String.format("接口[%s]有两个重复的BeanName[%s],且没有指定beanName，分别是[%s],[%s]",
                                anInterface.getName(), interfaceBeanName, ioc.get(beanName).getClass().getName(), aClass.getName()));
                    }
                    ioc.put(interfaceBeanName, o);
                }
            }
        }
    }

    private String firstLower(String simpleName) {
        return simpleName.substring(0, 1).toLowerCase() +
                simpleName.substring(1);
    }

    private void doScanner(String scanPackage) throws IOException {
        classes = ScannerUtils.getClasses(scanPackage);
    }

    private void doLoadConfig(ServletConfig config) {
        String configurationLocation = config.getInitParameter("configurationLocation");
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(configurationLocation);

        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive, Set<Class<?>> classes) {
        File dir = new File(packagePath);
        if (dir.exists() && dir.isDirectory()) {
            File[] dirfiles = dir.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    return recursive && file.isDirectory() || file.getName().endsWith(".class");
                }
            });
            File[] var6 = dirfiles;
            int var7 = dirfiles.length;

            for(int var8 = 0; var8 < var7; ++var8) {
                File file = var6[var8];
                if (file.isDirectory()) {
                    findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
                } else {
                    String className = file.getName().substring(0, file.getName().length() - 6);

                    try {
                        classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                    } catch (ClassNotFoundException var12) {
                        var12.printStackTrace();
                    }
                }
            }

        }
    }
}
