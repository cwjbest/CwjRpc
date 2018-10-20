package com.cwj.service;

/**
 * Created by cwj on 18-10-14.
 * 服务实现类
 */
public class HelloServiceImpl implements IHelloService {
    public String sayHello(String name) {
        System.out.println("hello " + name);
        return name == null ? "hello nobody" : "hello " + name;
    }

    @Override
    public String sayGuaPi(String name) {
        System.out.println("guapi " + name);
        return name == null ? "hello nobody" : "guapi " + name;
    }
}
