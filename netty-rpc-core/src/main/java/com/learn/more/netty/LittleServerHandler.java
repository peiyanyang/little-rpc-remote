package com.learn.more.netty;

import com.alibaba.fastjson.JSONObject;
import com.learn.more.annotation.RpcServerManager;
import com.learn.more.vo.RpcMessage;
import com.learn.more.vo.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
public class LittleServerHandler extends SimpleChannelInboundHandler<RpcMessage> {
    private RpcServerManager rpcServerManager;

    public LittleServerHandler(RpcServerManager rpcServerManager) {
        this.rpcServerManager = rpcServerManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage message) throws Exception {
        //获取客户端发送过来的消息
        log.info("客户端发送消息："+message);
        //todo 需要根据msq中带bean的名字，和方法名，与注解缓存起来的bean对象比较，获取到具体的业务逻辑接口类，调用对应的方法
        String beanName = message.getBeanName();
        String methodName = message.getMethodName();

        Object rpcServerBean = rpcServerManager.getRpcServerBean(beanName);
        RpcResponse response = null;

        response = new RpcResponse();
        response.setBusiId(message.getBusiId());
        try {
            // 反射 invoke
            Class<?> serviceClass = rpcServerBean.getClass();
            Class<?>[] parameterTypes = message.getParameterTypes();
            Object[] parameters = message.getParameters();

            Method method = serviceClass.getMethod(methodName, parameterTypes);
            method.setAccessible(true);

            int length = parameters.length;
            Object[] changeParas = new Object[length];
            for(int i = 0;i< length;i++){
                Object tempParams = JSONObject.parseObject(parameters[i].toString(),parameterTypes[i]);
                changeParas[i] = tempParams;
            }
            Object result = method.invoke(rpcServerBean, changeParas);

            response.setResult(result);
            log.info("处理返回的内容："+response.toString());
        } catch (Exception e) {
            log.error("执行{}类{}方法报错，异常：{}",beanName,methodName,e);
            response.setErrorMsg(e.getMessage());
        }

        ctx.writeAndFlush(response);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }
}
