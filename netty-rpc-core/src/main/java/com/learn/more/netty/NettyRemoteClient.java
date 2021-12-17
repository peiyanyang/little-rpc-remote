package com.learn.more.netty;

import com.learn.more.serialize.impl.JSONSerializer;
import com.learn.more.vo.NettyServerConfig;
import com.learn.more.vo.RpcMessage;
import com.learn.more.vo.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;

import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class NettyRemoteClient{
    private static Logger logger = LoggerFactory.getLogger(NettyRemoteClient.class);

    private Bootstrap bootstrap;
    private EventLoopGroup eventLoopGroup;

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

    public NettyServerConfig getNettyServerConfig() {
        return nettyServerConfig;
    }

    private NettyServerConfig nettyServerConfig;
    private static final int MAX_RETRY = 5;

    private Channel channel;

    public Channel getChannel() {
        return channel;
    }

    /**
     * 初始化客户端
     */
//    @PostConstruct
    public void initClient(NettyServerConfig nettyServerConfig) {
        this.bootstrap = new Bootstrap();
        this.eventLoopGroup = new NioEventLoopGroup();
        this.nettyServerConfig = nettyServerConfig;

        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
                .handler(
                        new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ChannelPipeline pipeline = ch.pipeline();
                                //TODO idle心跳还没加
                                pipeline.addLast(new IdleStateHandler(0, 0, 5, TimeUnit.SECONDS)).
                                        addLast(new RpcEncoder(RpcMessage.class, new JSONSerializer())).
                                        addLast(new RpcDecoder(RpcResponse.class, new JSONSerializer())).
                                        addLast(new LittleClientHandler(NettyRemoteClient.this));
                            }
                        }
                );

        try {
            connect(nettyServerConfig, MAX_RETRY);
        } catch (Exception e) {
            logger.error("rpc连接异常：{}", e);
        }
    }

    /**
     *
     * @param nettyServerConfig
     * @param retry
     * @throws InterruptedException
     */
    public void connect(NettyServerConfig nettyServerConfig, int retry){
        ChannelFuture channelFuture  = bootstrap.connect(nettyServerConfig.getHostName(), nettyServerConfig.getPort()).addListener(future -> {
            if (future.isSuccess()) {
                logger.info("连接服务端成功");
            } else if (retry == 0) {
                logger.error("重试次数已用完，放弃连接");
            } else {
                //第几次重连：
                int order = retry + 1;
                //本次重连的间隔
//                int delay = 1 << order;
                int delay = 5;
                logger.error("{} : 连接失败，第 {} 重连....", new Date(), order);
                bootstrap.config().group().schedule(() -> {
                    connect(nettyServerConfig, retry - 1);
                }, delay, TimeUnit.SECONDS);
            }
        });
        try {
            this.channel = channelFuture.sync().channel();
        } catch (Exception e) {
            logger.error("rpc连接异常：{}", e);
        }
    }

    /**
     * 发送消息
     * @param rpcMessage
     * @throws Exception
     */
    public ChannelFuture sendMessage(RpcMessage rpcMessage) throws Exception {
        //TODO 这里要加个listener
        ChannelFuture rpcFuture = this.channel.writeAndFlush(rpcMessage).sync();

        return rpcFuture;
    }

    @PreDestroy
    public void close() {
        eventLoopGroup.shutdownGracefully();
        channel.closeFuture().syncUninterruptibly();
    }

    /**
     * 在bean实例化后，初始化前，把bean的field替换成proxy对象
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
//    @Override
//    public boolean postProcessAfterInstantiation(final Object bean, final String beanName) throws BeansException {
//        ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
//            @Override
//            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
//                if (field.isAnnotationPresent(RpcClient.class)) {
//                    // valid
//                    Class iface = field.getType();
//                    if (!iface.isInterface()) {
//                        throw new RuntimeException("远程rpc调用的服务必须有接口");
//                    }
//
//                    //TODO 这里要用代理替换原来的Field
//
//                    RpcClientProxy proxy = new RpcClientProxy();
//
//                    // get proxyObj
//                    Object serviceProxy = null;
//                    try {
//                        serviceProxy = proxy.getProxyBean(iface, applicationContext);
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//
//                    // set bean
//                    field.setAccessible(true);
//                    field.set(bean, serviceProxy);
//
//                    logger.info("nettyRemoteClient初始化完成, bean.field = {}.{}", beanName, field.getName());
//
//
//                }
//            }
//        });
//        return true;
//    }


}
