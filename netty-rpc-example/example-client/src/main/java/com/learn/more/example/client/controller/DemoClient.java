package com.learn.more.example.client.controller;

import com.learn.more.annotation.RpcClient;
import com.learn.more.example.api.DemoService;
import com.learn.more.example.inout.QueryUser;
import com.learn.more.example.inout.UserDTO;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/test")
@RestController
public class DemoClient {

    @RpcClient
    private DemoService demoService;

    @PostMapping("/sayHello")
    @ResponseBody
    public UserDTO sayHello(@RequestBody QueryUser queryUser) {

        try {
            return demoService.sayHi(queryUser);
        } catch (Exception e) {
            e.printStackTrace();
            return new UserDTO(null, e.getMessage());
        }
    }
}
