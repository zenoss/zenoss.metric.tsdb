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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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
        client = new OpenTsdbClient(socket, 8192);
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
        final String message = OpenTsdbClient.toPutMessage("m", 0, 0.0, EMPTY_MAP);
        /*
         * The buffered output stream uses a padded array, an offset, and a length
         * for its calls to the real output stream. This comparison verifies that 
         * the call contains the expected message for provided offset and length
         */
        doAnswer(new BufferedWriteVerifier(message)).
            when(output).write(any(byte[].class), anyInt(), anyInt());
        
        client.put(message);
        client.flush();
        verify(output, times(1)).write(any(byte[].class), anyInt(), anyInt());
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
                byte[] stringBytes = response.getBytes(StandardCharsets.UTF_8);
                System.arraycopy(stringBytes, 0, message, 0, stringBytes.length);
                return stringBytes.length;
            }
        }).when(input).read(any(byte[].class));
        
        doAnswer(new BufferedWriteVerifier(response)).
            when(output).write(any(byte[].class), anyInt(), anyInt());
        
        assertEquals(response, client.version());
        verify(output, times(1)).write(any(byte[].class), anyInt(), anyInt());
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
    
    static class BufferedWriteVerifier implements Answer<Void> {

        private final String message;

        BufferedWriteVerifier(String message) {
            this.message = message;
        }
        
        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            byte[] buffered = (byte[]) invocation.getArguments()[0];
            int offest = (int) invocation.getArguments()[1];
            int len = (int) invocation.getArguments()[2];

            byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);
            for (int i=offest; i < len; i++) {
                assertEquals(msgBytes[i], buffered[i]);
            }
            return null;
        }
        
    }
}
