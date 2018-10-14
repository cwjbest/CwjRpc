package com.cwj.test;

import com.cwj.client.Client;
import com.cwj.server.IServer;
import com.cwj.server.ServerImpl;
import com.cwj.service.HelloServiceImpl;
import com.cwj.service.IHelloService;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by cwj on 18-10-14.
 *
 */
public class RpcTest {
    public static void main(String[] args) {
        new Thread(() -> {
            try {
                IServer server = new ServerImpl(8088);
                server.register(IHelloService.class, HelloServiceImpl.class);
                server.start();
            }catch (IOException e){
                e.printStackTrace();
            }
        }).start();

        IHelloService service = Client.getRemoteProxyObj(IHelloService.class,
                new InetSocketAddress("localhost", 8088));
        System.out.println(service.sayHello("You look stupid!"));
    }
}
