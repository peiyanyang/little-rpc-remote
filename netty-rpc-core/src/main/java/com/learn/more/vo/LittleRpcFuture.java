package com.learn.more.vo;

public class LittleRpcFuture {
    private String busiId;
    private RpcResponse rpcResponse;

    private volatile boolean isSucceed = false;
    private final Object object = new Object();

    public String getBusiId() {
        return busiId;
    }

    public void setBusiId(String busiId) {
        this.busiId = busiId;
    }

    public RpcResponse getRpcResponse(int timeout) {
        synchronized (object) {
            while (!isSucceed) {
                try {
                    object.wait(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return rpcResponse;
        }
    }

    public void setRpcResponse(RpcResponse rpcResponse) {
        if (isSucceed) {
            return;
        }
        synchronized (object) {
            this.rpcResponse = rpcResponse;
            this.isSucceed = true;
            object.notify();
        }
    }
}