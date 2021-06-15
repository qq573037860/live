package com.sjq.live.utils;

import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class PackageScanner {

    private static final ClassLoader cl = Thread.currentThread().getContextClassLoader();

    public static List<Class> scanInterfaceByPackagePathAndAnnotation(String packagePath, Class[] annotations) {
        final boolean annotationFilter = Objects.nonNull(annotations) && annotations.length > 0;
        try {
            return scanClassByPackagePath(packagePath, cls -> {
                if (!cls.isInterface() || cls.isAnnotation()) {
                    return false;
                }
                if (annotationFilter) {
                    for (Class annotationCls : annotations) {
                        if (Objects.isNull(cls.getAnnotation(annotationCls))) {
                            return false;
                        }
                    }
                }
                return true;
            });
        } catch (Exception e) {
            throw new RuntimeException("scanInterfaceByPackagePathAndAnnotation failed", e);
        }
    }

    public static List<Class> scanClassByPackagePathAndAnnotation(String packagePath, Class[] annotations) {
        final boolean annotationFilter = Objects.nonNull(annotations) && annotations.length > 0;
        try {
            return scanClassByPackagePath(packagePath, cls -> {
                if (cls.isInterface() || cls.isAnnotation()) {
                    return false;
                }
                if (annotationFilter) {
                    for (Class annotationCls : annotations) {
                        if (Objects.isNull(cls.getAnnotation(annotationCls))) {
                            return false;
                        }
                    }
                }
                return true;
            });
        } catch (Exception e) {
            throw new RuntimeException("scanClassByPackagePathAndAnnotation failed", e);
        }
    }

    public static List<Class> scanClassByPackagePath(final String packagePath,
                                                     final Function<Class, Boolean> filter) throws ClassNotFoundException, IOException {
        //File root = new File(PackageScanner.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceFirst("(?:file:)?/{1}", ""));
        final List<Class> classes = new ArrayList<>();

        //需要扫描的目录
        List<File> directories = new ArrayList<>();
        //项目的根路径
        List<String> rootPaths = new ArrayList<>();
        //查找文件资源
        final Enumeration<URL> fileUrls = Thread.currentThread().getContextClassLoader().getResources(packagePath.replaceAll("\\.", "/"));
        final Enumeration<URL> rootUrls = Thread.currentThread().getContextClassLoader().getResources("");
        //查找directories
        final String regex = "(?:file:)?/{1}";
        while (fileUrls.hasMoreElements()) {
            URL url = fileUrls.nextElement();
            File file = new File(url.getFile().replaceFirst(regex, ""));
            if (file.isDirectory()) {
                directories.add(file);
            }
        }
        //查找rootPaths
        while (rootUrls.hasMoreElements()) {
            URL url = rootUrls.nextElement();
            rootPaths.add(new File(url.getFile().replaceFirst(regex, "")).getPath());
        }

        //查找满足要求class
        for (;;) {
            List<File> directoriesTemp = new ArrayList<>();
            for (File parent : directories) {
                if (Objects.isNull(parent.listFiles()) || Objects.requireNonNull(parent.listFiles()).length == 0) {
                    continue;
                }
                for (File child : Objects.requireNonNull(parent.listFiles())) {
                    if (child.isDirectory()) {
                        directoriesTemp.add(child);
                        continue;
                    }
                    if (!child.getName().endsWith("class")) {
                        continue;
                    }

                    for (String rootPath : rootPaths) {
                        //剔除项目根路径
                        String path = child.getPath().replace(rootPath, "");
                        if (path.length() == child.getPath().length()) {
                            continue;
                        }
                        //转换成com.sjq.rpc这种格式,并加载class
                        Class<?> cls = cl.loadClass(path.substring(1, path.length() - 6).replace(File.separator, "."));
                        //过滤
                        if (filter.apply(cls)) {
                            classes.add(cls);
                        }
                        break;
                    }
                }
            }
            if (CollectionUtils.isEmpty(directoriesTemp)) {
                break;
            }
            directories.clear();
            directories = directoriesTemp;
        }

        return classes;
    }

}
