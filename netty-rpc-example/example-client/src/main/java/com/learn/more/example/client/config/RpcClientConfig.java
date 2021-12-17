package com.learn.more.example.client.config;

import com.learn.more.netty.NettyRemoteClient;
import com.learn.more.proxy.RpcClientInstantsFactory;
import com.learn.more.vo.NettyServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RpcClientConfig {
    private Logger logger = LoggerFactory.getLogger(RpcClientConfig.class);


    @Value("${little-rpc.host}")
    private String host;

    @Value("${little-rpc.port}")
    private Integer port;


    @Bean
    public RpcClientInstantsFactory rpcClientExecutor() {
        NettyServerConfig nettyServerConfig = new NettyServerConfig();
        nettyServerConfig.setHostName(host);
        nettyServerConfig.setPort(port);

        RpcClientInstantsFactory client = new RpcClientInstantsFactory();
        client.setNettyServerConfig(nettyServerConfig);

        logger.info(">>>>>>>>>>> little-rpc client 初始化完成.");
        return client;
    }

}