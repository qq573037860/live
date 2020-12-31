package com.sjq.live.endpoint.netty.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sjq.live.model.LiveException;
import com.sjq.live.support.spring.SpringBeanUtil;
import com.sjq.live.utils.PackageScanner;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NettyEndPointRegister {

    private static final Map<String, MethodInvokerHandler> METHOD_INVOKER_HOLDER_MAP = Maps.newHashMap();

    private static String buildKey(String path, HttpMethod method) {
        return Objects.isNull(method) ? null : String.format("%s_%s", path, method.name());
    }

    public static MethodInvokerHandler match(String path, HttpMethod method) {
        return METHOD_INVOKER_HOLDER_MAP.get(buildKey(path, method));
    }

    public static void register() {
        //扫描满足要求class
        final List<Class> classes = PackageScanner.scanClassByPackagePathAndAnnotation("", new Class[]{NettyEndPoint.class});
        //解析类信息
        for (Class<?> cls : classes) {
            //匹配需要绑定的方法
            NettyEndPoint clsAnnotation = cls.getAnnotation(NettyEndPoint.class);
            String prefixPath = clsAnnotation.path();
            List<Method> methods = Lists.newArrayList();
            Map<String, Method> endPointPathToMethodMap = Maps.newHashMap();
            for (Method method : cls.getMethods()) {
                NettyEndPoint methodAnnotation = method.getAnnotation(NettyEndPoint.class);
                if (Objects.nonNull(methodAnnotation)) {
                    if (endPointPathToMethodMap.containsKey(prefixPath + methodAnnotation.path())) {
                        throw new LiveException(String.format("EndPointPth[%s]已经存在", methodAnnotation.path()));
                    }
                    endPointPathToMethodMap.put(methodAnnotation.path(), method);
                    methods.add(method);
                }
            }

            if (CollectionUtils.isEmpty(methods)) {
                continue;
            }

            //生成wrapper
            EndpointWrapper endpointWrapper = EndpointWrapper.makeWrapper(cls, methods.toArray(new Method[0]));
            //实例化
            Object instance = getInstance(cls);
            //MethodInvokerHandler绑定
            for (Method method : methods) {
                NettyEndPoint methodAnnotation = method.getAnnotation(NettyEndPoint.class);
                String key = buildKey(methodAnnotation.path(), methodAnnotation.method());
                METHOD_INVOKER_HOLDER_MAP.put(key, new MethodInvokerHandler(endpointWrapper, instance, method.getName(), method.getParameterTypes()));
            }
        }
    }

    private static Object getInstance(Class<?> cls) {
        SpringBeanUtil.registerBean(cls);
        return SpringBeanUtil.getBean(cls);
    }

    public static class MethodInvokerHandler {

        private EndpointWrapper endpointWrapper;

        private Object instance;

        private String methodName;

        private Class<?>[] argsTypes;

        public Object invoke(Object[] args) {
            try {
                return endpointWrapper.invokeMethod(instance, methodName, argsTypes, args);
            } catch (Exception e) {
                throw new LiveException(e);
            }
        }

        public MethodInvokerHandler(EndpointWrapper endpointWrapper, Object instance, String methodName, Class<?>[] argsTypes) {
            this.endpointWrapper = endpointWrapper;
            this.instance = instance;
            this.methodName = methodName;
            this.argsTypes = argsTypes;
        }

        protected EndpointWrapper getEndpointWrapper() {
            return endpointWrapper;
        }

        protected void setEndpointWrapper(EndpointWrapper endpointWrapper) {
            this.endpointWrapper = endpointWrapper;
        }

        protected Object getInstance() {
            return instance;
        }

        protected void setInstance(Object instance) {
            this.instance = instance;
        }

        protected String getMethodName() {
            return methodName;
        }

        protected void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        protected Class<?>[] getArgsTypes() {
            return argsTypes;
        }

        protected void setArgsTypes(Class<?>[] argsTypes) {
            this.argsTypes = argsTypes;
        }
    }
}
