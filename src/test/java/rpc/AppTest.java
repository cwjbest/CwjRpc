package rpc;


import com.cwj.client.Client;
import com.cwj.service.IHelloService;
import org.junit.Test;

import java.net.InetSocketAddress;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    private IHelloService service = Client.getRemoteProxyObj(IHelloService.class,
            new InetSocketAddress("172.21.6.101", 6666));

    @Test
    public void sayHello(){
        service.sayHello("zhcong");
    }

    @Test
    public void sayGuaPi(){
        service.sayGuaPi("zhcong");
    }

}
