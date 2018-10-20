# CwjRpc

### 使用方法
1. 将工程导入idea
2. 执行RpcTest方法，注意端口占用问题
3. 或者找到target包下的rpc-1.0-SNAPSHOT-jar-with-dependencies.jar，在jar目录下执行
   java -jar rpc-1.0-SNAPSHOT-jar-with-dependencies.jar 端口号

### client
1. 将本地接口调用转换成JDK的动态代理，在动态代理中实现接口的调用
2. 创建SocketChannel客户端，根据指定地址调用远程服务的提供者
3. 将远程接口调用所需要的接口类，方法名，参数列表等编码后发送给服务者
4. Selector等待服务端返回应答，获取执行结果
### 为什么要用代理？
1. 没有DynamicProxy也能实现client发起接口服务的申请，server端实现真正调用。
   但是这就让client完全依赖于server端的对象了，没有server对象，client对象就没法存在，耦合性太强。
   而有了DynamicProxy，两者就完全解耦了。所以说DynamicProxy的定位就是给client和server端解耦的。
   
### Server
1. 作为服务端，监听客户端的TCP链接
2. 将客户端发送的码流反序列化成对象，反射调用服务的实现者，获取执行结果
3. 将结果反序列化，通过SocketChannel发送给客户端
4. 远程服务调用结束后，释放资源

### 一些问题
1. java原生的nio真是不太友好啊，API繁琐，编程步骤太冗余
2. 使用ByteBuffer传输数据时，不能直接传输Object，需要使用ByteArrayInputStream过度一下，详见代码

### 后续支持
1. 使用netty nio作为通信框架
2. 扩展至分布式，使用zookeeper做服务注册
3. java序列化性能太低，使用其他序列化框架

