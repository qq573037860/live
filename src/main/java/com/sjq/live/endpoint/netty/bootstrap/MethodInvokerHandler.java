package com.sjq.live.endpoint.netty.bootstrap;

import com.sjq.live.model.LiveException;

public class MethodInvokerHandler extends AbstractMethodInvokerHandler {

    private EndpointWrapper endpointWrapper;

    private String methodName;

    private Class<?>[] argsTypes;

    public Object invoke(Object[] args) {
        try {
            return endpointWrapper.invokeMethod(instance, methodName, argsTypes, args);
        } catch (Exception e) {
            throw new LiveException(e);
        }
    }

    public void invokeAsync(Object[] args) {
        EXECUTOR_SERVICE.execute(() -> {
            try {
                endpointWrapper.invokeMethod(instance, methodName, argsTypes, args);
            } catch (Exception e) {
                throw new LiveException(e);
            }
        });
    }

    public MethodInvokerHandler(EndpointWrapper endpointWrapper, Object instance, String methodName, Class<?>[] argsTypes) {
        this.instance = instance;
        this.endpointWrapper = endpointWrapper;
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
