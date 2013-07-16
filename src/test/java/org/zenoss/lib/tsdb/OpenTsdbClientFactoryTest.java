package org.zenoss.lib.tsdb;

import org.junit.Test;

import java.net.Socket;
import java.net.SocketAddress;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OpenTsdbClientFactoryTest {
    @Test
    public void testNewClient() throws Exception {
        Socket socket = mock(Socket.class);
        SocketAddress address = mock(SocketAddress.class);
        SocketFactory socketFactory = mock(SocketFactory.class);
        when(socketFactory.newSocket( any(SocketAddress.class))).thenReturn(socket);
        OpenTsdbClientFactory factory = new OpenTsdbClientFactory( socketFactory);
        assertNotNull( factory.newClient( address));
    }
}
