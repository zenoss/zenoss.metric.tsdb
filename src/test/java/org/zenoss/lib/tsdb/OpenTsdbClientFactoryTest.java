package org.zenoss.lib.tsdb;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OpenTsdbClientFactoryTest {
    
    private static final long MAX_KEEP_ALIVE = 50L;
    
    OpenTsdbClientPoolConfiguration config() {
        
        OpenTsdbClientConfiguration c1 = new OpenTsdbClientConfiguration();
        c1.setHost("localhost");
        c1.setPort(123);
        
        OpenTsdbClientPoolConfiguration c = new OpenTsdbClientPoolConfiguration();
        List<OpenTsdbClientConfiguration> clients = Collections.singletonList(c1);
        c.setClientConfiguration(clients);
        c.setMaxKeepAliveTime(MAX_KEEP_ALIVE);
        c.setMinTestTime(MAX_KEEP_ALIVE);
        return c;
    }
    
    @Test
    public void testNewClient() throws Exception {
        Socket socket = mock(Socket.class);
        SocketFactory socketFactory = mock(SocketFactory.class);
        when (socketFactory.newSocket (any (SocketAddress.class))).thenReturn(socket);
        
        OpenTsdbClientFactory factory = new OpenTsdbClientFactory(config(), socketFactory);
        assertNotNull (factory.makeObject());
    }
    
    @Test
    public void testBasicValidate() throws Exception {
        Socket socket = mock(Socket.class);
        SocketFactory socketFactory = mock(SocketFactory.class);
        OutputStream os = mock(OutputStream.class);
        InputStream is = mock(InputStream.class);
        when (socketFactory.newSocket (any (SocketAddress.class))).thenReturn(socket);
        
        OpenTsdbClientFactory factory = new OpenTsdbClientFactory(config(), socketFactory);
        OpenTsdbClient c1 = factory.makeObject();
        
        when (socket.getOutputStream()).thenReturn(os);
        when (socket.getInputStream()).thenReturn(is);
        when (is.read(any(byte[].class))).thenAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                byte[] message = (byte[]) invocation.getArguments()[0];
                byte[] version = "net.opentsdb built at revision 1.2.3".getBytes();
                System.arraycopy(version, 0, message, 0, version.length);
                return version.length;
            } 
        });
        
        assertEquals(Boolean.TRUE, factory.validateObject(c1));
    }
    
    @Test
    public void testMaxKeepAlive() throws Exception {
        Socket socket = mock(Socket.class);
        SocketFactory socketFactory = mock(SocketFactory.class);
        when (socketFactory.newSocket (any (SocketAddress.class))).thenReturn(socket);
        
        OpenTsdbClientFactory factory = new OpenTsdbClientFactory(config(), socketFactory);
        OpenTsdbClient c1 = factory.makeObject();
        
        Thread.sleep(MAX_KEEP_ALIVE + 1); // Longer than max keep alive
        
        assertEquals(Boolean.FALSE, factory.validateObject(c1));
        
        verify (socket, never()).getOutputStream();
        verify (socket, never()).getInputStream();
    }
    
    @Test
    public void testRecentlyTested() throws Exception {
        Socket socket = mock(Socket.class);
        SocketFactory socketFactory = mock(SocketFactory.class);
        when (socketFactory.newSocket (any (SocketAddress.class))).thenReturn(socket);
        
        OpenTsdbClientFactory factory = new OpenTsdbClientFactory(config(), socketFactory);
        OpenTsdbClient c1 = factory.makeObject();
        c1.updateTested();
        
        assertEquals(Boolean.TRUE, factory.validateObject(c1));
        
        verify (socket, never()).getOutputStream();
        verify (socket, never()).getInputStream();
    }
}
