package com.spring.learn;


import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @Author: zhuquanwen
 * @Description:
 * @Date: 2018/4/25 17:30
 * @Modified:
 **/

public class ScannerUtils {
//    public static void main(String[] args) throws IOException {
////        Set<Class<?>> sets = getClasses("com.iscas");
////        sets.stream().forEach(System.out::println);
////        String packageRealPath = getPackageRealPath("com.iscas.det");
////        System.out.println(packageRealPath);
////        File file = new File(packageRealPath);
////        System.out.println(file.getAbsolutePath());
//
//        String property = System.getProperty("user.dir");
//        System.out.println(property);
//
//        File directory = new File("");//参数为空
//        String courseFile = directory.getCanonicalPath() ;
//        System.out.println(courseFile);
//
//
//    }

    public static Set<Class<?>> getClasses(String pack){
        return getClasses(pack, true);
    }


    public static Set<Class<?>> getClasses(String pack, boolean ignore$){

        // 第一个class类的集合
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        // 是否循环迭代
        boolean recursive = true;
        // 获取包的名字 并进行替换
        String packageName = pack;
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try{
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()){
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    System.out.println("=============file类型的扫描");
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
                }else if ("jar".equals(protocol)) {
                    String packageName1 = "";
                    // 如果是jar包文件
                    // 定义一个JarFile

                    System.out.println("=============jar类型的扫描");

                    JarFile jar;
                    try{
                        // 获取jar
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        // 从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        // 同样的进行循环迭代
                        while (entries.hasMoreElements()){
                            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            // 如果是以/开头的
                            if (name.charAt(0) == '/') {
                                // 获取后面的字符串
                                name = name.substring(1);
                            }
                            // 如果前半部分和定义的包名相同
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                // 如果以"/"结尾 是一个包
                                if (idx != -1) {
                                    // 获取包名 把"/"替换成"."
                                    packageName1 = name.substring(0, idx).replace('/', '.');
                                }
                                // 如果可以迭代下去 并且是一个包
                                if ((idx != -1) || recursive) {
                                    // 如果是一个.class文件 而且不是目录
                                    if (name.endsWith(".class") && !entry.isDirectory() && !name.contains("$")) {
                                        // 去掉后面的".class" 获取真正的类名
                                        String className = name.substring(packageName1.length() + 1, name.length() - 6);
                                        Class<?> aClass = null;
                                        try{
                                            // 添加到classes
                                            aClass = Class.forName(packageName1 + '.' + className);
                                        }catch (Exception e){
                                            System.out.println("加载类出错");
                                        }
                                        if (aClass != null) {
                                            classes.add(aClass);
                                        }
                                    }
                                }
                            }
                        }
                    }catch (IOException e){
                        // log.error("在扫描用户定义视图时从jar包获取文件出错");
                        e.printStackTrace();
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return classes;
    }

    public static void findAndAddClassesInPackageByFile(
            String packageName,
            String packagePath,
            final boolean recursive,
            Set<Class<?>> classes){
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            // log.warn("用户定义包名 " + packageName + " 下没有任何文件");
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirfiles = dir.listFiles(new FileFilter(){

            // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            @Override
            public boolean accept(File file){
                return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        // 循环所有文件
        for (File file : dirfiles){
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
            }else{
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try{
                    // 添加到集合中去
                    // classes.add(Class.forName(packageName + '.' +
                    // className));
                    // 经过回复同学的提醒，这里用forName有一些不好，会触发static方法，没有使用classLoader的load干净
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                }catch (ClassNotFoundException e){
                    // log.error("添加用户自定义视图类错误 找不到此类的.class文件");
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getPackageRealPath(String packageName){
        String packageDirName = packageName.replace('.', '/');
        return Thread.currentThread().getContextClassLoader().getResource(packageDirName).getFile();
    }

}