package org.zenoss.lib.tsdb;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

/**
 * Created with IntelliJ IDEA.
 * User: scleveland
 * Date: 7/2/13
 * Time: 1:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class SocketFactoryTest {

    @Test(expected=UnknownHostException.class)
    public void testNewSocket() throws Exception {
        SocketFactoryConfiguration configuration = new SocketFactoryConfiguration();
        SocketFactory factory = new SocketFactory( configuration);
        SocketAddress address = new InetSocketAddress( "unknown-host111111", 1);
        factory.newSocket(address);
    }

    @Test(expected=UnknownHostException.class)
    public void testNewSocket_() throws Exception {
        SocketFactoryConfiguration configuration = new SocketFactoryConfiguration();
        configuration.setSoTimeout(1);
        configuration.setConnectTimeout(-1);
        configuration.setReceiveBufferSize(1);
        configuration.setSendBufferSize(1);
        SocketFactory factory = new SocketFactory( configuration);
        SocketAddress address = new InetSocketAddress( "unknown-host111111", 1);
        factory.newSocket(address);
    }
}
