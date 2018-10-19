package com.cwj.test;

import com.cwj.server.IServer;
import com.cwj.server.ServerImpl;
import com.cwj.service.HelloServiceImpl;
import com.cwj.service.IHelloService;

import java.io.IOException;

/**
 * Created by cwj on 18-10-14.
 *
 */
public class RPCServer {
    public static void main(String[] args) {
        new Thread(() -> {
            try {
                IServer server = new ServerImpl(Integer.valueOf(args[0]));
                server.register(IHelloService.class, HelloServiceImpl.class);
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
