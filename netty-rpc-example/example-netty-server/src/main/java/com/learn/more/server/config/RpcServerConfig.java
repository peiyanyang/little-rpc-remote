package com.learn.more.server.config;

import com.learn.more.annotation.RpcServerManager;
import com.learn.more.netty.NettyRemoteServer;
import com.learn.more.vo.NettyServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RpcServerConfig {
    private Logger logger = LoggerFactory.getLogger(RpcServerConfig.class);


    @Value("${little-rpc.host}")
    private String host;

    @Value("${little-rpc.port}")
    private Integer port;

    @Bean
    public NettyRemoteServer rpcServerExecutor(ApplicationContext applicationContext,RpcServerManager rpcServerManager) {
        NettyServerConfig nettyServerConfig = new NettyServerConfig();
        nettyServerConfig.setHostName(host);
        nettyServerConfig.setPort(port);

        NettyRemoteServer server = new NettyRemoteServer(nettyServerConfig, rpcServerManager);

        logger.info(">>>>>>>>>>> little-rpc server 初始化完成.");
        return server;
    }

}