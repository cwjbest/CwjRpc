package com.cwj.service;

/**
 * Created by cwj on 18-10-14.
 * 服务实现类
 */
public class HelloServiceImpl implements IHelloService {
    public String sayHello(String name) {
        return name == null ? "hello nobody" : "hello " + name;
    }
}
