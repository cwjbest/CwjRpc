package com.cwj.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by cwj on 18-10-14.
 * 服务发布者的主要职责
 * <p>
 * 作为服务端，监听客户端的TCP链接，接收到客户端的饿链接之后封装成Tsak交给线程池执行
 * 将客户端发送的码流反序列化成对象，反射调用服务的实现者，获取执行结果
 * 将结果反序列化，通过Socket发送给客户端
 * 远程服务调用结束后，释放资源
 */
public class ServerImpl implements IServer {
    //返回jvm可用虚拟机的数量，也就是当前cpu有几个核,就创建固定数目的线程
    private static ExecutorService executor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    //服务注册表
    public static final HashMap<String, Class> serviceRegistry = new HashMap<String, Class>();
    private static boolean isRunning = false;
    private int port;

    public ServerImpl(int port) {
        this.port = port;
    }


    /**
     * 使用多线程，主要原因在于socket.accept()、socket.read()、socket.write()三个主要函数都是同步阻塞的，
     * 当一个连接在处理I/O的时候，系统是阻塞的，如果是单线程的话必然就挂死在那里；但CPU是被释放出来的，开启多线程，
     * 就可以让CPU去处理更多的事情。其实这也是所有使用多线程的本质：利用多核。当I/O阻塞系统，但CPU空闲的时候，
     * 可以利用多线程使用CPU资源。
     * @throws IOException
     */
    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(port));
        System.out.println("start server!");
        try {
            while (true) {
                ////接收到客户端的连接后封装成task交给线程池
                //这儿的accept是阻塞的，没办法知道能不能读，只能傻等
                executor.execute(new ServiceTask(serverSocket.accept()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            serverSocket.close();
        }
    }

    public void stop() {
        isRunning = false;
        executor.shutdown();
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

    private static class ServiceTask implements Runnable {
        Socket client = null;

        public ServiceTask(Socket client) {
            this.client = client;
        }

        public void run() {
            ObjectOutputStream output = null;
            ObjectInputStream input = null;

            try {
                //接收client发送的数据
                //read操作是阻塞的，BIO无法知道能不能读，即使不能读，也会一直占着线程，无法返回
                input = new ObjectInputStream(client.getInputStream());
                String serviceName = input.readUTF();
                String methodName = input.readUTF();
                Class<?>[] parameterTypes = (Class<?>[]) input.readObject();
                Object[] arguments = (Object[]) input.readObject();

                //取出服务
                Class serviceClass = serviceRegistry.get(serviceName);
                if (serviceClass == null)
                    throw new ClassNotFoundException(serviceName + " not found");

                Method method = serviceClass.getMethod(methodName, parameterTypes);

                if (method.getName().equals("sayHello")) {
                    System.out.println("hello");
                }
                if (method.getName().equals("sayGuaPi"))
                    System.out.println("guapi");

                //调用方法
                Object result = method.invoke(serviceClass.newInstance(), arguments);
                //返回结果发送给client
                output = new ObjectOutputStream(client.getOutputStream());
                output.writeObject(result);
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
    }
}
