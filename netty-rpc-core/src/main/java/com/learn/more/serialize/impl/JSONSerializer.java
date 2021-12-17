package com.learn.more.serialize.impl;

import com.alibaba.fastjson.JSON;
import com.learn.more.serialize.Serializer;

import java.io.IOException;

public class JSONSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) throws IOException {
        return JSON.toJSONString(object).getBytes();
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) throws IOException {
        return JSON.parseObject(bytes, clazz);
    }
}
