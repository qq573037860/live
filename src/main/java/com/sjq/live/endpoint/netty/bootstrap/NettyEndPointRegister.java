package com.sjq.live.endpoint.netty.bootstrap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sjq.live.model.LiveException;
import com.sjq.live.support.spring.SpringBeanUtil;
import com.sjq.live.utils.PackageScanner;
import com.sjq.live.utils.proxy.Wrapper;
import org.springframework.asm.Type;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class NettyEndPointRegister {

    /**
     * url与handler的映衬MAP
     */
    private static final Map<String, Object> METHOD_INVOKER_HOLDER_MAP = Maps.newHashMap();

    /**
     * 实现NettyWebsocketEndPointHandler的方法SET
     */
    private static final Class<NettyWebsocketEndPointHandler> WEBSOCKET_END_POINT_HANDLER_CLASS = NettyWebsocketEndPointHandler.class;
    private static final Set<String> WS_HANDLER_METHOD_DESCRIPTION_SET = Sets.newHashSet();
    private static final String METHOD_DESCRIPTION_FORMAT = "%s#%s";

    static {
        for (Method method : WEBSOCKET_END_POINT_HANDLER_CLASS.getMethods()) {
            WS_HANDLER_METHOD_DESCRIPTION_SET.add(getMethodDescription(method));
        }
    }

    private static String getMethodDescription(Method method) {
        return String.format(METHOD_DESCRIPTION_FORMAT, method.getName(), Type.getMethodDescriptor(method));
    }

    private static String buildKey(String path, HttpMethod method) {
        return Objects.isNull(method) ? null : String.format("%s_%s", path, method.name());
    }

    public static <T> T match(String path, HttpMethod method) {
        return (T) METHOD_INVOKER_HOLDER_MAP.get(buildKey(path, method));
    }

    public static <T> T match(String path) {
        return (T) METHOD_INVOKER_HOLDER_MAP.get(path);
    }

    private static boolean isImplementNettyWebsocketInterface(Class<?> cls) {
        for (Class<?> anInterface : cls.getInterfaces()) {
            if (anInterface == WEBSOCKET_END_POINT_HANDLER_CLASS) {
                return true;
            }
        }
        return false;
    }
    
    public static void register() {
        //扫描满足要求class
        final List<Class> classes = PackageScanner.scanClassByPackagePathAndAnnotation("", new Class[]{NettyEndPoint.class});
        //解析类信息
        for (Class<?> cls : classes) {
            //是否是实现WebsocketEndPointHandler
            boolean isImplementNettyWebsocketInterface = isImplementNettyWebsocketInterface(cls);

            //匹配需要绑定的方法
            String prefixPath = cls.getAnnotation(NettyEndPoint.class).path();
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
                if (isImplementNettyWebsocketInterface && WS_HANDLER_METHOD_DESCRIPTION_SET.contains(getMethodDescription(method))) {
                    methods.add(method);
                }
            }

            if (CollectionUtils.isEmpty(methods)) {
                continue;
            }

            //InvokerHandler绑定
            for (Method method : methods) {
                NettyEndPoint methodAnnotation = method.getAnnotation(NettyEndPoint.class);
                if (Objects.nonNull(methodAnnotation)) {
                    String key = buildKey(prefixPath + methodAnnotation.path(), methodAnnotation.method());
                    METHOD_INVOKER_HOLDER_MAP.put(key, new NettyHttpEndPointHandlerProxy(Wrapper.makeWrapper(cls, methods.toArray(new Method[0])),
                            SpringBeanUtil.getBean(cls), method.getName(), method.getParameterTypes()));
                }
            }
            if (isImplementNettyWebsocketInterface) {
                METHOD_INVOKER_HOLDER_MAP.put(prefixPath, new NettyWebsocketEndPointHandlerProxy(SpringBeanUtil.getBean((Class<NettyWebsocketEndPointHandler>)cls)));
            }
        }
    }
}
