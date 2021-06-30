package com.sjq.live.support.spring;

import com.sjq.live.utils.proxy.ClassUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.function.Supplier;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;

@Component
public class SpringBeanUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    private static BeanDefinitionRegistry registry;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
        registry = (BeanDefinitionRegistry) applicationContext;
    }

    public static <T> BeanFactory<T> getBean(Class<T> tClass) {
        return Objects.isNull(applicationContext) ? new BeanFactory<>(() -> applicationContext.getBean(tClass)) : new BeanFactory<>(applicationContext.getBean(tClass));
    }

    public static Object getBean(String name) {
        return Objects.isNull(applicationContext) ? new BeanFactory<>(() -> applicationContext.getBean(name)) : new BeanFactory<>(applicationContext.getBean(name));
    }

    public static void registerBean(Class<?> cls) {
        registerBean(cls, null, (BeanDefinitionRegistry) applicationContext);
    }

    public static void registerBean(Class<?> cls, Object constructorArgValue, BeanDefinitionRegistry registry) {
        if (!registry.containsBeanDefinition(ClassUtils.humpClassName(cls))) {
            BeanDefinitionBuilder builder = rootBeanDefinition(cls);
            if (Objects.nonNull(constructorArgValue)) {
                builder.addConstructorArgValue(constructorArgValue);
            }
            builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
            BeanDefinitionReaderUtils.registerWithGeneratedName(beanDefinition, registry);
        }
    }

    public static class BeanFactory<T> {

        T bean;

        Supplier<T> beanFactory;

        BeanFactory(T bean) {
            this.bean = bean;
        }

        BeanFactory(Supplier<T> beanFactory) {
            this.beanFactory = beanFactory;
        }

        public T getBean() {
            if (Objects.isNull(bean)) {
                bean = beanFactory.get();
            } else if (Objects.nonNull(beanFactory)) {
                beanFactory = null;
            }
            return bean;
        }
    }
}
