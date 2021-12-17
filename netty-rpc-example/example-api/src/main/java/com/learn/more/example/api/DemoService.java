package com.learn.more.example.api;


import com.learn.more.example.inout.QueryUser;
import com.learn.more.example.inout.UserDTO;

/**
 * Demo API
 */
public interface DemoService {

	public UserDTO sayHi(QueryUser queryUser);

}
