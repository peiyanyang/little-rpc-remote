package com.learn.more.netty;

import com.learn.more.utils.RpcFutureManager;
import com.learn.more.vo.LittleRpcFuture;
import com.learn.more.vo.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class LittleClientHandler extends ChannelInboundHandlerAdapter {
    private NettyRemoteClient nettyRemoteClient;

    public LittleClientHandler(NettyRemoteClient nettyRemoteClient) {
        this.nettyRemoteClient = nettyRemoteClient;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("服务端连接成功");
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //todo 读取服务端的返回信息，根据id查找到对应的future，返回结果
        RpcResponse message = (RpcResponse) msg;
        String busiId = message.getBusiId();
        LittleRpcFuture responseFuture = RpcFutureManager.getRpcRequestFuture(busiId);
        //填充结果
        responseFuture.setRpcResponse(message);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.error("与服务端失去连接");
        //使用过程中断线重连
        final EventLoop eventLoop = ctx.channel().eventLoop();
        eventLoop.schedule(() -> {
            log.info("客户端重连中......");
            //一直尝试重连
            nettyRemoteClient.connect(nettyRemoteClient.getNettyServerConfig(),Integer.MAX_VALUE-1);
        }, 1L, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
