package com.sjq.live.endpoint.netty.bootstrap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sjq.live.endpoint.netty.websocket.NettyPublishVideoStreamEndpointEndPoint;
import com.sjq.live.model.LiveException;
import com.sjq.live.support.spring.SpringBeanUtil;
import com.sjq.live.utils.PackageScanner;
import org.springframework.asm.Type;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class NettyEndPointRegister {

    private static final Map<String, AbstractMethodInvokerHandler> METHOD_INVOKER_HOLDER_MAP = Maps.newHashMap();
    private static final Set<String> WS_HANDLER_METHOD_DESCRIPTION_SET = Sets.newHashSet();
    private static final String METHOD_DESCRIPTION_FORMAT = "%s#%s";

    static {
        for (Method method : NettyWebsocketEndPointHandler.class.getMethods()) {
            WS_HANDLER_METHOD_DESCRIPTION_SET.add(getMethodDescription(method));
        }
    }

    private static String getMethodDescription(Method method) {
        return String.format(METHOD_DESCRIPTION_FORMAT, method.getName(), Type.getMethodDescriptor(method));
    }

    private static String buildKey(String path, HttpMethod method) {
        return Objects.isNull(method) ? null : String.format("%s_%s", path, method.name());
    }

    private static String buildKey(String path, String methodDescription) {
        return String.format("%s_%s", path, methodDescription);
    }

    public static <T> T match(String path, HttpMethod method) {
        return (T) METHOD_INVOKER_HOLDER_MAP.get(buildKey(path, method));
    }

    public static <T> T match(String path) {
        return (T) METHOD_INVOKER_HOLDER_MAP.get(path);
    }

    private static boolean isImplementNettyWebsocketInterface(Class cls) {
        for (Class<?> anInterface : NettyPublishVideoStreamEndpointEndPoint.class.getInterfaces()) {
            return anInterface == NettyWebsocketEndPointHandler.class;
        }
        return false;
    }
    
    public static void register() {
        //扫描满足要求class
        final List<Class> classes = PackageScanner.scanClassByPackagePathAndAnnotation("", new Class[]{NettyEndPoint.class});
        //解析类信息
        for (Class<?> cls : classes) {
            //匹配需要绑定的方法
            boolean isImplementNettyWebsocketInterface = isImplementNettyWebsocketInterface(cls);
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
                if (isImplementNettyWebsocketInterface
                        && WS_HANDLER_METHOD_DESCRIPTION_SET.contains(getMethodDescription(method))) {
                    methods.add(method);
                }
            }

            if (CollectionUtils.isEmpty(methods)) {
                continue;
            }

            //实例化
            Object instance = getInstance(cls);
            //InvokerHandler绑定
            for (Method method : methods) {
                NettyEndPoint methodAnnotation = method.getAnnotation(NettyEndPoint.class);
                if (Objects.nonNull(methodAnnotation)) {
                    String key = buildKey(prefixPath + methodAnnotation.path(), methodAnnotation.method());
                    METHOD_INVOKER_HOLDER_MAP.put(key, new MethodInvokerHandler(EndpointWrapper.makeWrapper(cls, methods.toArray(new Method[0])), instance, method.getName(), method.getParameterTypes()));
                }
            }
            if (isImplementNettyWebsocketInterface) {
                METHOD_INVOKER_HOLDER_MAP.put(prefixPath, new WebsocketMethodInvokerEndPointHandler(instance));
            }
        }
    }

    private static Object getInstance(Class<?> cls) {
        //SpringBeanUtil.registerBean(cls);
        return SpringBeanUtil.getBean(cls);
    }
}
