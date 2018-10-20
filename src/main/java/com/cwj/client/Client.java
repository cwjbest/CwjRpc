package com.cwj.client;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by cwj on 18-10-14.
 * 本地服务的代理的功能如下
 * <p>
 * 将本地接口调用转换成JDK的动态代理，在动态代理中实现接口的调用
 * 创建Socket客户端，根据指定地址调用远程服务的提供者
 * 将远程接口调用所需要的接口类，方法名，参数列表等编码后发送给服务者
 * 同步阻塞等待服务端返回应答，获取执行结果
 * <p>
 * 为什么要用代理？
 * 没有DynamicProxy也能实现client发起接口服务的申请，server端实现真正调用。
 * 但是这就让client完全依赖于server端的对象了，没有server对象，client对象就没法存在，耦合性太强。
 * 而有了DynamicProxy，两者就完全解耦了。所以说DynamicProxy的定位就是给client和server端解耦的。
 */

@SuppressWarnings("unchecked")
public class Client {
    public static <T> T getRemoteProxyObj(final Class<?> serviceInterface, final InetSocketAddress addr) {
        return (T) Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class<?>[]{serviceInterface},
                new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        //用channel代替socket，其实也就是对socket做了封装
                        SocketChannel socketChannel = null;

                        //序列化传递对象，所以使用Object装饰类
                        ObjectInputStream in = null;
                        ObjectOutputStream out = null;

                        try {
                            socketChannel = SocketChannel.open(addr);
                            //false表示非阻塞IO
                            socketChannel.configureBlocking(false);
                            Selector selector = Selector.open();
                            socketChannel.register(selector, SelectionKey.OP_READ);

                            System.out.println("start send...");
                            //将接口，方法，参数打包发送到server
                            ByteArrayOutputStream bas = new ByteArrayOutputStream();
                            out = new ObjectOutputStream(bas);
                            out.writeObject(method.getParameterTypes());
                            out.writeObject(args);

                            //这里不能在一个流中同时使用writeUTF和writeObject，必须统一
                            out.writeObject(serviceInterface.getName());
                            out.writeObject(method.getName());

                            //ByteBuffer buffer = ByteBuffer.allocate(1024);
                            //buffer.wrap(out.toByteArray());
                            //很奇怪，使用对象来调用wrap静态方法，会出现很奇怪的错误
                            //按说static方法,类和实例都能调才对啊，没搞懂
                            ByteBuffer sendBuffer = ByteBuffer.wrap(bas.toByteArray());
                            System.out.println("capacity: " + sendBuffer.capacity());
                            socketChannel.write(sendBuffer);

                            System.out.println("sent");

                            //接收server反馈对象
                            while (selector.select() > 0) {
                                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                                System.out.println("received ack...");
                                while (it.hasNext()) {
                                    SelectionKey key = it.next();
                                    SocketChannel ackChannel = (SocketChannel) key.channel();
                                    ByteBuffer ackBuffer = ByteBuffer.allocate(29);
                                    ackChannel.read(ackBuffer);
                                    ByteArrayInputStream bai = new ByteArrayInputStream(ackBuffer.array());
                                    in = new ObjectInputStream(bai);
                                    return in.readObject();
                                }
                                it.remove();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (socketChannel != null) socketChannel.close();
                            if (in != null) in.close();
                            if (out != null) out.close();
                        }
                        return null;
                    }
                });
    }
}
