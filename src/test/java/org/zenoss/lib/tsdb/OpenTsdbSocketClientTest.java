package org.zenoss.lib.tsdb;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OpenTsdbSocketClientTest {

    static final Map<String, String> EMPTY_MAP = Collections.emptyMap();

    @Mock
    OpenTsdbClientConfiguration configuration;

    @Mock
    Socket socket;

    @Mock
    InputStream input;

    @Mock
    OutputStream output;

    OpenTsdbClient client;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(socket.getInputStream()).thenReturn(input);
        when(socket.getOutputStream()).thenReturn(output);
        when(configuration.newSocket()).thenReturn(socket);
        client = new OpenTsdbClient(configuration);
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
        String message = OpenTsdbClient.toPutMessage("m", 0, 0.0, EMPTY_MAP);
        client.put(message);
        verify(output, times(1)).write(message.getBytes());
    }

    @Test(expected=IOException.class)
    public void testRead() throws IOException {
        client.read();
    }
}
