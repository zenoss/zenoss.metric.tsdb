package com.zenoss.metric.tsdb;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TsdbSocketClientTest {

    @Mock
    Socket socket;

    @Mock
    SocketAddress address;

    @Mock
    InputStream input;

    @Mock
    OutputStream output;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(socket.getRemoteSocketAddress()).thenReturn(address);
        when(socket.getInputStream()).thenReturn(input);
        when(socket.getOutputStream()).thenReturn(output);
    }

    @Test(expected = IOException.class)
    public void testOpenWithHostAndPort() throws Exception {
        TsdbSocketClient client = new TsdbSocketClient("127.0.0.1", 0);
        client.open();
    }

    @Test(expected = IOException.class)
    public void testOpenWithSocketAddress() throws Exception {
        TsdbSocketClient client = new TsdbSocketClient(new InetSocketAddress("127.0.0.1", 0));
        client.open();
    }

    @Test
    public void testClose() {
        try {
            TsdbSocketClient client = new TsdbSocketClient(socket);
            client.close();
            client.close();
            verify(socket, times(1)).close();
        } catch (IOException ex) {
            ex.printStackTrace();
            fail();
        }
    }


    @Test
    public void testSleep() throws Exception {
        //test non-interrupted scenario
        assertFalse(Thread.currentThread().isInterrupted());
        TsdbSocketClient.sleep(1);
        assertFalse(Thread.currentThread().isInterrupted());

        //test interrupted scenario
        Thread.currentThread().interrupt();
        assertTrue(Thread.currentThread().isInterrupted());
        TsdbSocketClient.sleep(1);
        assertTrue(Thread.currentThread().interrupted());
    }

    @Test
    public void testToPutMessageWithNoTags() throws Exception {
        Metric metric = new Metric("m", 0, 0.0);
        assertEquals("put m 0 0.0\n", TsdbSocketClient.toPutMessage(metric));
    }

    @Test
    public void testToPutMessageWithTags() throws Exception {
        Metric metric = new Metric("m", 0, 0.0);
        metric.setTag("h", "h");
        assertEquals("put m 0 0.0 h=h\n", TsdbSocketClient.toPutMessage(metric));
    }

    @Test
    public void testPutSuccess() throws Exception {
        Metric metric = new Metric("m", 0, 0.0);
        String message = TsdbSocketClient.toPutMessage(metric);
        TsdbSocketClient client = new TsdbSocketClient(socket);
        when(input.available()).thenReturn(0);
        try {
            client.put(metric);
            verify(output, times(1)).write(message.getBytes());
            verify(output, times(1)).flush();
            verify(input, times(5)).available();
        } catch (IOException | TsdbException ex) {
            ex.printStackTrace();
            fail();
        }
    }

    @Test(expected=TsdbConnectionClosedException.class)
    public void testPutThrowsConnectionClosedException() throws Exception {
        Metric metric = new Metric("m", 0, 0.0);
        TsdbSocketClient client = new TsdbSocketClient(socket);
        when(input.available()).thenReturn(-1);
        client.put(metric);
    }

    @Test(expected=TsdbException.class)
    public void testPutThrowsTsdbException() throws Exception {
        final Metric metric = new Metric("m", 0, 0.0);
        final TsdbSocketClient client = new TsdbSocketClient(socket);
        final String response = "put: failed";
        when(input.available()).thenReturn(response.getBytes().length);
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                byte[] bytes = response.getBytes();
                byte[] arg = (byte[]) invocation.getArguments()[0];
                System.arraycopy(bytes, 0, arg, 0, arg.length);
                return null;
            }
        }).when(input).read(any( byte[].class));
        client.put(metric);
    }
}
