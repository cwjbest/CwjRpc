package com.cwj.server;

import java.io.IOException;

/**
 * Created by cwj on 18-10-14.
 * server接口
 */
public interface IServer {
    void start() throws IOException;
    void stop();
    void register(Class serviceInterface, Class impl);
    boolean isRunning();
    int getPort();
}
