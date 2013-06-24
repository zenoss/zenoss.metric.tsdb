package org.zenoss.metric.tsdb;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OpenTsdbSocketClientTest {

    static final Map<String, String> EMPTY_MAP = Collections.emptyMap();

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
        OpenTsdbSocketClient client = new OpenTsdbSocketClient("127.0.0.1", 0);
        client.open();
    }

    @Test(expected = IOException.class)
    public void testOpenWithSocketAddress() throws Exception {
        OpenTsdbSocketClient client = new OpenTsdbSocketClient(new InetSocketAddress("127.0.0.1", 0));
        client.open();
    }

    @Test
    public void testClose() {
        try {
            OpenTsdbSocketClient client = new OpenTsdbSocketClient(socket);
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
        OpenTsdbSocketClient.sleep(1);
        assertFalse(Thread.currentThread().isInterrupted());

        //test interrupted scenario
        Thread.currentThread().interrupt();
        assertTrue(Thread.currentThread().isInterrupted());
        OpenTsdbSocketClient.sleep(1);
        assertTrue(Thread.currentThread().interrupted());
    }

    @Test
    public void testPutSuccess() throws Exception {
        OpenTsdbSocketClient client = new OpenTsdbSocketClient(socket);
        when(input.available()).thenReturn(0);

        try {
            String message = OpenTsdbSocketClient.toPutMessage("m", 0, 0.0, EMPTY_MAP);
            client.put(message);
            verify(output, times(1)).write(message.getBytes());
            verify(output, times(1)).flush();
            verify(input, times(5)).available();
        } catch (IOException | OpenTsdbException ex) {
            ex.printStackTrace();
            fail();
        }
    }

    @Test(expected=OpenTsdbConnectionClosedException.class)
    public void testPutThrowsConnectionClosedException() throws Exception {
        OpenTsdbSocketClient client = new OpenTsdbSocketClient(socket);
        when(input.available()).thenReturn(-1);
        String message = OpenTsdbSocketClient.toPutMessage("m", 0, 0.0, EMPTY_MAP);
        client.put(message);
    }

    @Test(expected=OpenTsdbException.class)
    public void testPutThrowsTsdbException() throws Exception {
        final OpenTsdbSocketClient client = new OpenTsdbSocketClient(socket);
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
        String message = OpenTsdbSocketClient.toPutMessage("m", 0, 0.0, EMPTY_MAP);
        client.put(message);
    }
}
