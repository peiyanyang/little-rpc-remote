package com.learn.more.utils;

import com.learn.more.vo.LittleRpcFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class RpcFutureManager {
    private static Logger logger = LoggerFactory.getLogger(RpcFutureManager.class);

    private static ConcurrentHashMap<String, LittleRpcFuture> rpcRequestFutureMap = new ConcurrentHashMap();
    public static void saveRpcRequestFuture(String busiId, LittleRpcFuture requestFuture){
        rpcRequestFutureMap.putIfAbsent(busiId, requestFuture);
    }
    public static LittleRpcFuture getRpcRequestFuture(String busiId) throws InterruptedException {
        return rpcRequestFutureMap.get(busiId);
    }
    public static LittleRpcFuture removeRpcRequestFuture(String busiId) throws InterruptedException {
        return rpcRequestFutureMap.remove(busiId);
    }
}
