/*
 * ****************************************************************************
 *
 *  Copyright (C) Zenoss, Inc. 2014, all rights reserved.
 *
 *  This content is made available according to terms specified in
 *  License.zenoss distributed with this file.
 *
 * ***************************************************************************
 */
package org.zenoss.lib.tsdb;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
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
        InputStream is = new ByteArrayInputStream("net.opentsdb built at revision 1.2.3".getBytes(StandardCharsets.UTF_8));
        when (socketFactory.newSocket (any (SocketAddress.class))).thenReturn(socket);
        
        OpenTsdbClientFactory factory = new OpenTsdbClientFactory(config(), socketFactory);
        OpenTsdbClient c1 = factory.makeObject();
        
        when (socket.getOutputStream()).thenReturn(os);
        when (socket.getInputStream()).thenReturn(is);

        assertTrue(factory.validateObject(c1));
    }
    
    @Test
    public void testReadError() throws Exception {
        Socket socket = mock(Socket.class);
        SocketFactory socketFactory = mock(SocketFactory.class);
        OutputStream os = mock(OutputStream.class);
        InputStream is = new ByteArrayInputStream("Fake Error!".getBytes(StandardCharsets.UTF_8));
        when (socketFactory.newSocket (any (SocketAddress.class))).thenReturn(socket);
        
        OpenTsdbClientFactory factory = new OpenTsdbClientFactory(config(), socketFactory);
        OpenTsdbClient c1 = factory.makeObject();
        
        when (socket.getOutputStream()).thenReturn(os);
        when (socket.getInputStream()).thenReturn(is);

        assertFalse( factory.hasCollision());
        assertFalse( factory.validateObject(c1));
        assertFalse( factory.hasCollision());
        assertEquals( 1, factory.clearErrorCount());
        assertEquals( 0, factory.clearErrorCount());
    }


    @Test
    public void testReadCollisionError() throws Exception {
        Socket socket = mock(Socket.class);
        SocketFactory socketFactory = mock(SocketFactory.class);
        OutputStream os = mock(OutputStream.class);
        when (socketFactory.newSocket (any (SocketAddress.class))).thenReturn(socket);

        OpenTsdbClientFactory factory = new OpenTsdbClientFactory(config(), socketFactory);
        OpenTsdbClient c1 = factory.makeObject();

        String message = "put: HBase error: 1000 RPCs waiting on \"tsdb,,1398325180794.54ad8182f2f2a0a1cc6d39ba26ca7f64.\" to come back online";
        InputStream is = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));

        when (socket.getOutputStream()).thenReturn(os);
        when (socket.getInputStream()).thenReturn(is);

        assertFalse( factory.hasCollision());
        assertFalse( factory.validateObject(c1));
        assertTrue( factory.hasCollision());
        assertFalse( factory.hasCollision());
        assertEquals( 1, factory.clearErrorCount());
        assertEquals( 0, factory.clearErrorCount());
    }

    @Test
    public void testReadIOException() throws Exception {
        Socket socket = mock(Socket.class);
        SocketFactory socketFactory = mock(SocketFactory.class);
        OutputStream os = mock(OutputStream.class);
        InputStream is = mock(InputStream.class);
        when (socketFactory.newSocket (any (SocketAddress.class))).thenReturn(socket);
        
        OpenTsdbClientFactory factory = new OpenTsdbClientFactory(config(), socketFactory);
        OpenTsdbClient c1 = factory.makeObject();
        
        when (socket.getOutputStream()).thenReturn(os);
        when (socket.getInputStream()).thenReturn(is);
        when (is.read(any(byte[].class))).thenThrow(new IOException());
        
        assertFalse(factory.validateObject(c1));
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
    
    static class ByteArrayWriter implements Answer<Integer> {
        
        private int count;
        private Object[] responses;
        
        ByteArrayWriter(Object... responses) {
            this.responses = responses;
            this.count = 0;
        }
        
        @Override
        public Integer answer(InvocationOnMock invocation) throws Throwable {
            assertTrue("Did not provide enough responses", responses.length > count);
            
            byte[] message = (byte[]) invocation.getArguments()[0];
            Object response = responses[count++];
            if (response instanceof Throwable) {
                throw (Throwable) response;
            }
            byte[] reply = String.class.cast(response).getBytes(StandardCharsets.UTF_8);
            System.arraycopy(reply, 0, message, 0, reply.length);
            return reply.length;
        }
        
    }
}
