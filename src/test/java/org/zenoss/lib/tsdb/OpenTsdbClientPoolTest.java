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

import org.fest.util.Lists;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OpenTsdbClientPoolTest {

    /*
     * Test that when we borrow, we delegate to the factory, which
     * validates the client before returning it.
     */
    @Test
    public void testBorrow() throws Exception {
        OpenTsdbClientFactory factory = mock(OpenTsdbClientFactory.class);
        OpenTsdbClientPool pool = new OpenTsdbClientPool(config(), factory);
        OpenTsdbClient client = mock (OpenTsdbClient.class);
        
        when (factory.makeObject()).thenReturn (client);
        when (factory.validateObject (client)).thenReturn (Boolean.TRUE);
        
        assertSame (client, pool.borrowObject());
        
        verify(client, never()).close();
        verify(factory, times(1)).validateObject(client);
    }
    
    @Test (expected = NoSuchElementException.class)
    public void testBorrow_ValidateFails() throws Exception {
        OpenTsdbClientFactory factory = mock (OpenTsdbClientFactory.class);
        OpenTsdbClientPool pool = new OpenTsdbClientPool(config(), factory);
        OpenTsdbClient client = mock(OpenTsdbClient.class);
        
        when (factory.makeObject()).thenReturn (client);
        when (factory.validateObject (client)).thenReturn (Boolean.FALSE);
        
        pool.borrowObject(); // Should fail!
    }

    @Test
    public void testReturnsLastUsedClient() throws Exception {
        OpenTsdbClientPoolConfiguration configuration = config2();
        OpenTsdbClientFactory factory = mock(OpenTsdbClientFactory.class);
        OpenTsdbClientPool pool = new OpenTsdbClientPool(configuration, factory);
        OpenTsdbClient c1 = mock(OpenTsdbClient.class);
        OpenTsdbClient c2 = mock(OpenTsdbClient.class);
        
        when (factory.makeObject()).thenReturn(c1, c2);
        when (factory.validateObject(c1)).thenReturn(Boolean.TRUE);
        when (factory.validateObject(c2)).thenReturn(Boolean.TRUE);
        
        assertSame(c1, pool.borrowObject());
        pool.returnObject(c1);
        Thread.sleep(100);
        
        assertSame(c1, pool.borrowObject());
        
        verify(factory, never()).destroyObject(c1);
        verify(factory, never()).destroyObject(c2);
    }

    @Test
    public void testReturnsAliveClient() throws Exception {
        OpenTsdbClientPoolConfiguration configuration = config2();
        OpenTsdbClientFactory factory = mock(OpenTsdbClientFactory.class);
        OpenTsdbClientPool pool = new OpenTsdbClientPool(configuration, factory);
        OpenTsdbClient c1 = mock(OpenTsdbClient.class);
        OpenTsdbClient c2 = mock(OpenTsdbClient.class);
        
        when (factory.makeObject()).thenReturn(c1, c2);
        when (factory.validateObject(c1)).thenReturn(true, false);
        when (factory.validateObject(c2)).thenReturn(true);
        
        assertSame(c1, pool.borrowObject());
        pool.returnObject(c1);
        Thread.sleep(100);
        assertSame(c2, pool.borrowObject());
        
        verify(factory, times(1)).destroyObject(c1);
        verify(factory, never()).destroyObject(c2);
    }

    @Test
    public void testReturnsNextClient() throws Exception {
        OpenTsdbClientPoolConfiguration configuration = config2();
        OpenTsdbClientFactory factory = mock(OpenTsdbClientFactory.class);
        OpenTsdbClientPool pool = new OpenTsdbClientPool(configuration, factory);
        OpenTsdbClient c1 = mock(OpenTsdbClient.class);
        OpenTsdbClient c2 = mock(OpenTsdbClient.class);
        
        when (factory.makeObject()).thenReturn(c1, c2);
        when (factory.validateObject(c1)).thenReturn(Boolean.TRUE);
        when (factory.validateObject(c2)).thenReturn(Boolean.TRUE);
        
        assertSame(c1, pool.borrowObject());
        assertSame(c2, pool.borrowObject());
        
        verify(factory, never()).destroyObject(c1);
        verify(factory, never()).destroyObject(c2);
    }

    @Test
    public void testGetBlocksAndInterrupts() throws Exception {
        final OpenTsdbClientPoolConfiguration configuration = new OpenTsdbClientPoolConfiguration();
        final OpenTsdbClientFactory factory = mock(OpenTsdbClientFactory.class);
        final OpenTsdbClientPool pool = new OpenTsdbClientPool(configuration, factory);
        
        final AtomicBoolean didInterrupt = new AtomicBoolean(false);
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    OpenTsdbClient client = pool.borrowObject();
                } 
                catch (InterruptedException ex) {
                    didInterrupt.set(true);
                }
                catch (Exception e) {
                    // Something else!
                }
            }
        };
        assertFalse (didInterrupt.get());
        thread.start();
        thread.interrupt();
        thread.join();
        assertTrue (didInterrupt.get());
    }
    
    OpenTsdbClientPoolConfiguration config() {
        
        OpenTsdbClientConfiguration c1 = new OpenTsdbClientConfiguration();
        c1.setHost("localhost");
        c1.setPort(123);
        
        OpenTsdbClientPoolConfiguration c = new OpenTsdbClientPoolConfiguration();
        List<OpenTsdbClientConfiguration> clients = Collections.singletonList(c1);
        c.setClientConfiguration(clients);
        
        return c;
    }
    
    OpenTsdbClientPoolConfiguration config2() {
        
        OpenTsdbClientConfiguration c1 = new OpenTsdbClientConfiguration();
        c1.setHost("localhost");
        c1.setPort(123);
        
        OpenTsdbClientConfiguration c2 = new OpenTsdbClientConfiguration();
        c2.setHost("localhost");
        c2.setPort(123);
        
        OpenTsdbClientPoolConfiguration c = new OpenTsdbClientPoolConfiguration();
        List<OpenTsdbClientConfiguration> clients = Lists.newArrayList(c1, c2);
        c.setClientConfiguration(clients);
        c.setMinTestTime(1);
        c.setMaxKeepAliveTime(60000);
        
        return c;
    }
}
