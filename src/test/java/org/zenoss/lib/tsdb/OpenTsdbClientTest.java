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
import java.net.Socket;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OpenTsdbClientTest {

    static final Map<String, String> EMPTY_MAP = Collections.emptyMap();

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
        client = new OpenTsdbClient(socket);
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


    @Test
    public void testFlush() throws IOException {
        client.flush();
        verify(output, times(1)).flush();
    }

    @Test
    public void testRead() throws IOException {
        final String response = "response\n";
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                byte[] message = (byte[]) invocation.getArguments()[0];
                System.arraycopy(response.getBytes(), 0, message, 0, response.length());
                return response.getBytes().length;
            }
        }).when(input).read(any(byte[].class));
        assertEquals(response, client.read());
    }

    @Test
    public void testReadReturnsNull() throws IOException {
        assertNull(client.read());
    }

    @Test
    public void testVersion() throws IOException {
        final String response = "version\n";
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                byte[] message = (byte[]) invocation.getArguments()[0];
                System.arraycopy(response.getBytes(), 0, message, 0, response.length());
                return response.getBytes().length;
            }
        }).when(input).read(any(byte[].class));
        assertEquals(response, client.version());
        verify(output, times(1)).write("version\n".getBytes());
    }


    @Test(expected = IOException.class)
    public void testVersionThrows() throws IOException {
        client.version();
        verify(output, times(1)).write("version\n".getBytes());
    }

    @Test
    public void testIsAlive() throws IOException {
        final String response = "net.opentsdb built at revision \n";
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                byte[] message = (byte[]) invocation.getArguments()[0];
                System.arraycopy(response.getBytes(), 0, message, 0, response.length());
                return response.getBytes().length;
            }
        }).when(input).read(any(byte[].class));
        assertTrue(client.isAlive());
    }

    @Test
    public void testIsNotAliveWhenVersionMisMatch() throws IOException {
        final String response = "SDJFL built at revision \n";
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                byte[] message = (byte[]) invocation.getArguments()[0];
                System.arraycopy(response.getBytes(), 0, message, 0, response.length());
                return response.getBytes().length;
            }
        }).when(input).read(any(byte[].class));
        assertFalse(client.isAlive());
    }

    @Test
    public void testIsNotAliveWhenThrows() throws IOException {
        doThrow(new IOException()).when(input).read(any(byte[].class));
        assertFalse(client.isAlive());
    }
}
