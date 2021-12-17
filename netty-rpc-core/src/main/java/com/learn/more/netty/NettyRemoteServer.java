package com.learn.more.netty;

import com.learn.more.annotation.RpcServerManager;
import com.learn.more.serialize.impl.JSONSerializer;
import com.learn.more.vo.NettyServerConfig;
import com.learn.more.vo.RpcMessage;
import com.learn.more.vo.RpcResponse;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class NettyRemoteServer{
    private static Logger logger = LoggerFactory.getLogger(NettyRemoteServer.class);

    private final ServerBootstrap serverBootstrap;
    private final EventLoopGroup eventLoopGroupWorker;
    private final EventLoopGroup eventLoopGroupBoss;
    private final NettyServerConfig nettyServerConfig;
    private RpcServerManager rpcServerManager;


    public NettyRemoteServer(NettyServerConfig config, RpcServerManager rpcServerManager) {
        serverBootstrap = new ServerBootstrap();
        eventLoopGroupBoss = new NioEventLoopGroup(1, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("Netty_Remote_Boss_%d", this.threadIndex.incrementAndGet()));
            }
        });
        //worker线程数设置成cpu核数 * 2
        eventLoopGroupWorker = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("Netty_Remote_Worker_%d", this.threadIndex.incrementAndGet()));
            }
        });
        nettyServerConfig = config;
        this.rpcServerManager = rpcServerManager;
    }

    @PostConstruct
    public void init(){
        try {
            serverBootstrap.group(eventLoopGroupBoss, eventLoopGroupWorker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>(){

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new IdleStateHandler(0, 0, 5, TimeUnit.SECONDS)) //心跳
                                .addLast(new RpcEncoder(RpcResponse.class, new JSONSerializer()))
                                .addLast(new RpcDecoder(RpcMessage.class, new JSONSerializer()))
                                .addLast(new LittleServerHandler(rpcServerManager)); //这里执行自己的逻辑
                        }
                    });

            ChannelFuture channelFuture = serverBootstrap.bind(nettyServerConfig.getHostName(),nettyServerConfig.getPort()).sync();

            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("连接中断，异常：{}", e);
        }finally {
            eventLoopGroupBoss.shutdownGracefully();
            eventLoopGroupWorker.shutdownGracefully();
        }

    }
}
