package org.zenoss.lib.tsdb;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.*;

public class OpenTsdbClientPoolTest {


    @Test
    public void testRevivesAClient() throws Exception {
        OpenTsdbClientPoolConfiguration configuration = new OpenTsdbClientPoolConfiguration();
        OpenTsdbClientFactory factory = mock(OpenTsdbClientFactory.class);
        OpenTsdbClientPool pool = new OpenTsdbClientPool(configuration, factory);

        SocketAddress address = new InetSocketAddress("localhost", 128);
        pool.add(address);

        OpenTsdbClient client = mock(OpenTsdbClient.class);
        when(client.isAlive()).thenReturn(true);
        when(factory.newClient(address)).thenReturn(client);
        assertSame(client, pool.get());
        verify(client, never()).close();
        verify(client, times(1)).isAlive();
    }

    @Test
    public void testReturnsLastUsedClient() throws Exception {
        OpenTsdbClientPoolConfiguration configuration = new OpenTsdbClientPoolConfiguration();
        OpenTsdbClientFactory factory = mock(OpenTsdbClientFactory.class);
        configuration.setMinTestTime(1);
        configuration.setMaxKeepAliveTime(60000);
        OpenTsdbClientPool pool = new OpenTsdbClientPool(configuration, factory);

        SocketAddress address = new InetSocketAddress("localhost", 128);
        pool.add(address);
        pool.add(address);

        OpenTsdbClient c1 = mock(OpenTsdbClient.class);
        OpenTsdbClient c2 = mock(OpenTsdbClient.class);
        when(c1.isAlive()).thenReturn(true);
        when(c2.isAlive()).thenReturn(true);
        when(factory.newClient(address)).thenReturn(c1, c2);
        assertSame(c1, pool.get());
        pool.put(c1);
        Thread.sleep(100);
        assertSame(c1, pool.get());
        verify(c1, times(2)).isAlive();
        verify(c1, never()).close();
        verify(c2, never()).isAlive();
    }

    @Test
    public void testReturnsAliveClient() throws Exception {
        OpenTsdbClientPoolConfiguration configuration = new OpenTsdbClientPoolConfiguration();
        OpenTsdbClientFactory factory = mock(OpenTsdbClientFactory.class);
        configuration.setMinTestTime(1);
        configuration.setMaxKeepAliveTime(60000);
        OpenTsdbClientPool pool = new OpenTsdbClientPool(configuration, factory);

        SocketAddress address = new InetSocketAddress("localhost", 128);
        pool.add(address);
        pool.add(address);

        OpenTsdbClient c1 = mock(OpenTsdbClient.class);
        OpenTsdbClient c2 = mock(OpenTsdbClient.class);
        when(c1.isAlive()).thenReturn(true, false);
        when(c2.isAlive()).thenReturn(true);
        when(factory.newClient(address)).thenReturn(c1, c2);
        assertSame(c1, pool.get());
        pool.put(c1);
        Thread.sleep(100);
        assertSame(c2, pool.get());
        verify(c1, times(2)).isAlive();
        verify(c1, times(1)).close();
        verify(c2, times(1)).isAlive();
    }

    @Test
    public void testReturnsNextClient() throws Exception {
        OpenTsdbClientPoolConfiguration configuration = new OpenTsdbClientPoolConfiguration();
        OpenTsdbClientFactory factory = mock(OpenTsdbClientFactory.class);
        configuration.setMinTestTime(1);
        configuration.setMaxKeepAliveTime(1);
        OpenTsdbClientPool pool = new OpenTsdbClientPool(configuration, factory);

        SocketAddress address = new InetSocketAddress("localhost", 128);
        pool.add(address);
        pool.add(address);

        OpenTsdbClient c1 = mock(OpenTsdbClient.class);
        OpenTsdbClient c2 = mock(OpenTsdbClient.class);
        when(c1.isAlive()).thenReturn(true);
        when(c2.isAlive()).thenReturn(true);
        when(factory.newClient(address)).thenReturn(c1, c2);
        assertSame(c1, pool.get());
        pool.put(c1);
        Thread.sleep(100);
        assertSame(c2, pool.get());
        verify(c1, times(1)).isAlive();
        verify(c1, times(1)).close();
        verify(c2, times(1)).isAlive();
    }

    @Test
    public void testReturnsNextClientAfterKill() throws Exception {
        OpenTsdbClientPoolConfiguration configuration = new OpenTsdbClientPoolConfiguration();
        OpenTsdbClientFactory factory = mock(OpenTsdbClientFactory.class);
        configuration.setMinTestTime(1);
        configuration.setMaxKeepAliveTime(1);
        OpenTsdbClientPool pool = new OpenTsdbClientPool(configuration, factory);

        SocketAddress address = new InetSocketAddress("localhost", 128);
        pool.add(address);
        pool.add(address);

        OpenTsdbClient c1 = mock(OpenTsdbClient.class);
        OpenTsdbClient c2 = mock(OpenTsdbClient.class);
        when(c1.isAlive()).thenReturn(true);
        when(c2.isAlive()).thenReturn(true);
        when(factory.newClient(address)).thenReturn(c1, c2);
        assertSame(c1, pool.get());
        //executing the different paths of kill and put
        pool.kill(null);
        pool.kill(c1);
        pool.kill(c1);
        pool.put(null);
        pool.put(c1);
        Thread.sleep(100);
        assertSame(c2, pool.get());
        verify(c1, times(1)).isAlive();
        verify(c1, times(1)).close();
        verify(c2, times(1)).isAlive();
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
                    OpenTsdbClient client = pool.get();
                } catch (InterruptedException ex) {
                    didInterrupt.set(true);
                }
            }
        };
        assertFalse( didInterrupt.get());
        thread.start();
        thread.interrupt();
        thread.join();
        assertTrue(didInterrupt.get());
    }
}
