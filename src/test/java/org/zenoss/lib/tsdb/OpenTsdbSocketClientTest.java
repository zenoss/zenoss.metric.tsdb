package org.zenoss.lib.tsdb;

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
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OpenTsdbSocketClientTest {

    static final Map<String, String> EMPTY_MAP = Collections.emptyMap();

    @Mock
    OpenTsdbSocketClientConfiguration configuration;

    @Mock
    Socket socket;

    @Mock
    InputStream input;

    @Mock
    OutputStream output;

    OpenTsdbSocketClient client;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(socket.getInputStream()).thenReturn(input);
        when(socket.getOutputStream()).thenReturn(output);
        when(configuration.newSocket()).thenReturn(socket);
        client = new OpenTsdbSocketClient(configuration);
        client.open();
    }

    @Test
    public void testClose() {
        try {
            client.close();
            client.close();
            verify(socket, times(1)).close();
        } catch (IOException ex) {
            ex.printStackTrace();
            fail();
        }
    }


    @Test
    public void testPut() throws IOException {
        String message = OpenTsdbSocketClient.toPutMessage("m", 0, 0.0, EMPTY_MAP);
        client.put(message);
        verify(output, times(1)).write(message.getBytes());
    }

    @Test(expected=IOException.class)
    public void testRead() throws IOException {
        client.read();
    }
}
