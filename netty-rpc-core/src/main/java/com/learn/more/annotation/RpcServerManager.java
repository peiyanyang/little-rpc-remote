package com.learn.more.annotation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RpcServerManager implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    private ConcurrentHashMap<String, Object> rpcServerBeanMap = new ConcurrentHashMap<>();


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init(){
        initRpcServerBean(applicationContext);
    }

    /**
     * 解析注解
     * @param applicationContext
     */
    private void initRpcServerBean(ApplicationContext applicationContext){
        if (applicationContext == null) {
            return;
        }

        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcServer.class);

        if (serviceBeanMap!=null && serviceBeanMap.size()>0) {
            for (Object serviceBean : serviceBeanMap.values()) {
                // valid
                if (serviceBean.getClass().getInterfaces().length ==0) {
                    throw new RuntimeException("服务："+serviceBean.getClass().getName()+"没有对应的接口，服务端提供的服务必须要实现接口");
                }
                String interfaceName = serviceBean.getClass().getInterfaces()[0].getName();

                rpcServerBeanMap.putIfAbsent(interfaceName, serviceBean);
            }
        }
    }

    /**
     * 获取相应的服务提供类
     * @param beanName
     * @return
     */
    public Object getRpcServerBean(String beanName){
        return rpcServerBeanMap.get(beanName);
    }
}
