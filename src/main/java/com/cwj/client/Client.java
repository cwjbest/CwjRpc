package com.cwj.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by cwj on 18-10-14.
 * 本地服务的代理的功能如下
 * <p>
 * 将本地接口调用转换成JDK的动态代理，在动态代理中实现接口的调用
 * 创建Socket客户端，根据指定地址调用远程服务的提供者
 * 将远程接口调用所需要的接口类，方法名，参数列表等编码后发送给服务者
 * 同步阻塞等待服务端返回应答，获取执行结果
 *
 * 为什么要用代理？
 * 没有DynamicProxy也能实现client发起接口服务的申请，server端实现真正调用。
 * 但是这就让client完全依赖于server端的对象了，没有server对象，client对象就没法存在，耦合性太强。
 * 而有了DynamicProxy，两者就完全解耦了。所以说DynamicProxy的定位就是给client和server端解耦的。
 */

@SuppressWarnings("unchecked")
public class Client {
    public static <T> T getRemoteProxyObj(final Class<?> serviceInterface, final InetSocketAddress addr){
        return (T) Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class<?>[]{serviceInterface},
                new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Socket socket = null;
                        //序列化传递对象，所以使用Object装饰类
                        ObjectInputStream input = null;
                        ObjectOutputStream output = null;

                        try {
                            socket = new Socket();
                            socket.connect(addr);

                            //将接口，方法，参数打包发送到server
                            output = new ObjectOutputStream(socket.getOutputStream());
                            output.writeUTF(serviceInterface.getName());
                            output.writeUTF(method.getName());
                            output.writeObject(method.getParameterTypes());
                            output.writeObject(args);

                            //接收server反馈的对象
                            input = new ObjectInputStream(socket.getInputStream());
                            return input.readObject();
                        }catch (Exception e){
                            e.printStackTrace();
                        }finally {
                            if (socket != null) socket.close();
                            if (output != null) output.close();
                            if (input != null) input.close();
                        }
                        return null;
                    }
                });
    }
}
