package com.cwj.test;

import com.cwj.client.Client;
import com.cwj.service.IHelloService;

import java.net.InetSocketAddress;

/**
 * Created by cwj on 18-10-14.
 *
 */
public class RpcClient {
    public static void main(String[] args) {
        IHelloService service = Client.getRemoteProxyObj(IHelloService.class,
                new InetSocketAddress("localhost", 6666));
        System.out.println(service.sayHello("You look stupid!"));
    }
}
