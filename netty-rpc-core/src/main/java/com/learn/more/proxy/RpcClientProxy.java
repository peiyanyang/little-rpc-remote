package com.learn.more.proxy;

import com.alibaba.fastjson.JSONObject;
import com.learn.more.netty.NettyRemoteClient;
import com.learn.more.utils.RpcFutureManager;
import com.learn.more.vo.LittleRpcFuture;
import com.learn.more.vo.NettyServerConfig;
import com.learn.more.vo.RpcMessage;
import com.learn.more.vo.RpcResponse;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.UUID;

public class RpcClientProxy {
    private static Logger logger = LoggerFactory.getLogger(RpcClientProxy.class);

    private NettyRemoteClient nettyRemoteClient;

    /**
     * 初始化nettRemoteClient
     * @param config
     */
    private void initNettyRemoteClient(NettyServerConfig config){
        if (this.nettyRemoteClient == null){
            try {
                this.nettyRemoteClient = NettyRemoteClient.class.newInstance();
                nettyRemoteClient.initClient(config);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 生成rpc的代理类
     * @param serivceClass
     * @param config
     * @return
     */
    public Object getProxyBean(final Class<?> serivceClass, NettyServerConfig config) {

        initNettyRemoteClient(config);

        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{serivceClass}, (proxy, method, args) -> {

                    // method param
                    String className = method.getDeclaringClass().getName();
                    String methodName = method.getName();
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    Object[] parameters = args;
                    Class<?> returnType = method.getReturnType();

                    String busiId = UUID.randomUUID().toString();

                    RpcMessage rpcMessage = new RpcMessage();
                    rpcMessage.setBeanName(className);
                    rpcMessage.setBusiId(busiId);
                    rpcMessage.setMethodName(methodName);
                    rpcMessage.setParameterTypes(parameterTypes);
                    rpcMessage.setParameters(parameters);

                    logger.info("请求内容: {}",rpcMessage);

                    //注册回调，等到完成时获取结果
                    LittleRpcFuture rpcFuture = new LittleRpcFuture();
                    rpcFuture.setBusiId(busiId);
                    RpcFutureManager.saveRpcRequestFuture(busiId, rpcFuture);

                    ChannelFuture future = nettyRemoteClient.sendMessage(rpcMessage);

                    future.await();

                    LittleRpcFuture littleRpcFuture = RpcFutureManager.getRpcRequestFuture(rpcMessage.getBusiId());

                    RpcResponse response = littleRpcFuture.getRpcResponse(5000);

                    logger.info("请求调用返回结果：{}", response.getResult());

                    //获取到结果后，清除掉调用
                    RpcFutureManager.removeRpcRequestFuture(rpcMessage.getBusiId());

                    if (response.getErrorMsg() != null) {
                        throw new RuntimeException(response.getErrorMsg());
                    }

                    return JSONObject.parseObject(response.getResult().toString(),returnType);
//                    return response.getResult();
                });
    }

}
