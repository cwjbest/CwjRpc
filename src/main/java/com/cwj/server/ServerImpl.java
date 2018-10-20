package com.cwj.server;

import java.io.*;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by cwj on 18-10-14.
 * 服务发布者的主要职责
 * <p>
 * 作为服务端，监听客户端的TCP链接
 * 将客户端发送的码流反序列化成对象，反射调用服务的实现者，获取执行结果
 * 将结果反序列化，通过Socket发送给客户端
 * 远程服务调用结束后，释放资源
 */
public class ServerImpl implements IServer {
    public static final HashMap<String, Class> serviceRegistry = new HashMap<>();
    private static boolean isRunning = false;
    private int port;

    public ServerImpl(int port) {
        this.port = port;
    }

    public void start() throws IOException {

        ServerSocketChannel server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.bind(new InetSocketAddress(port));

        Selector selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("start receive...");
        while (selector.select() > 0){
            //获取已接受到的事件
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();

            while (it.hasNext()){
                SelectionKey key = it.next();
                if (key.isAcceptable()) {
                    //获取client发送过来的socket
                    SocketChannel client = server.accept();

                    client.configureBlocking(false);

                    //注册读事件
                    client.register(selector, SelectionKey.OP_READ);
                }else if (key.isReadable()){
                    ObjectInputStream input = null;
                    ObjectOutputStream output = null;
                    //获取client
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(178);
                    try {
                        client.read(buffer);
                        input = new ObjectInputStream(
                                new ByteArrayInputStream(buffer.array()));

                        Class<?>[] parameterTypes = (Class<?>[]) input.readObject();
                        Object[] arguments = (Object[]) input.readObject();
                        String serviceName = (String) input.readObject();
                        String methodName = (String) input.readObject();

                        //取出服务
                        Class serviceClass = serviceRegistry.get(serviceName);
                        if (serviceClass == null)
                            throw new ClassNotFoundException(serviceName + " not found");

                        Method method = serviceClass.getMethod(methodName, parameterTypes);

                        //调用方法
                        Object result = method.invoke(serviceClass.newInstance(), arguments);

                        //返回结果发送给client
                        ByteArrayOutputStream bas = new ByteArrayOutputStream();
                        output = new ObjectOutputStream(bas);
                        output.writeObject(result);
                        ByteBuffer ackBuffer = ByteBuffer.wrap(bas.toByteArray());
                        System.out.println("ack buffer length " + ackBuffer.limit());
                        client.write(ackBuffer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (output != null) {
                            try {
                                output.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (input != null) {
                            try {
                                input.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (client != null) {
                            try {
                                client.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                //这个事件已经选择过，删除掉
                it.remove();
            }
        }
    }

    //服务注册
    public void register(Class serviceInterface, Class impl) {
        serviceRegistry.put(serviceInterface.getName(), impl);
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getPort() {
        return port;
    }

    @Override
    public void stop() {
        isRunning = false;
    }
}
