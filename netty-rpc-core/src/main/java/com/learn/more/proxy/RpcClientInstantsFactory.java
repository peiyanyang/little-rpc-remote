package com.learn.more.proxy;

import com.learn.more.annotation.RpcClient;
import com.learn.more.vo.NettyServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class RpcClientInstantsFactory implements InstantiationAwareBeanPostProcessor {
    private static Logger logger = LoggerFactory.getLogger(RpcClientInstantsFactory.class);

    private NettyServerConfig nettyServerConfig;

    public NettyServerConfig getNettyServerConfig() {
        return nettyServerConfig;
    }

    public void setNettyServerConfig(NettyServerConfig nettyServerConfig) {
        this.nettyServerConfig = nettyServerConfig;
    }

    /**
     * 在bean实例化后，初始化前，把bean的field替换成proxy对象
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public boolean postProcessAfterInstantiation(final Object bean, final String beanName) throws BeansException {
        ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                if (field.isAnnotationPresent(RpcClient.class)) {
                    // valid
                    Class iface = field.getType();
                    if (!iface.isInterface()) {
                        throw new RuntimeException("远程rpc调用的服务必须有接口");
                    }

                    //TODO 这里要用代理替换原来的Field

                    RpcClientProxy proxy = new RpcClientProxy();

                    // get proxyObj
                    Object serviceProxy = null;
                    try {
                        serviceProxy = proxy.getProxyBean(iface, nettyServerConfig);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    // set bean
                    field.setAccessible(true);
                    field.set(bean, serviceProxy);

                    logger.info("nettyRemoteClient初始化完成, bean.field = {}.{}", beanName, field.getName());


                }
            }
        });
        return true;
    }


}
