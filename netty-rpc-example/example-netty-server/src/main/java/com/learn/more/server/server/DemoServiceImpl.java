package com.learn.more.server.server;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.learn.more.annotation.RpcServer;
import com.learn.more.example.api.DemoService;
import com.learn.more.example.inout.QueryUser;
import com.learn.more.example.inout.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@RpcServer
@Service
public class DemoServiceImpl implements DemoService {
    private Logger logger = LoggerFactory.getLogger(DemoServiceImpl.class);

    @Override
    public UserDTO sayHi(QueryUser queryUser) {
        logger.info("远程调用接口，传入的参数："+queryUser.toString());
        UserDTO userDTO = new UserDTO();
        userDTO.setName(queryUser.getName());
        userDTO.setWord(JSONArray.toJSONString(queryUser.getWords()));

        return userDTO;
    }
}
