package org.zenoss.lib.tsdb;

import org.apache.commons.pool.BasePoolableObjectFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import static org.zenoss.lib.tsdb.OpenTsdbClientPool.log;

/**
 *  A factory for creating OpenTsdbClients.
 */
public class OpenTsdbClientFactory extends BasePoolableObjectFactory<OpenTsdbClient> {

    public OpenTsdbClientFactory(OpenTsdbClientPoolConfiguration configuration) {
        this(configuration, new SocketFactory (configuration.getClientFactoryConfiguration()));
    }
    
    public OpenTsdbClientFactory(OpenTsdbClientPoolConfiguration configuration, SocketFactory socketFactory) {
        this.socketFactory = socketFactory;
        this.addresses = new LinkedList<>();
        this.maxKeepAliveTime = configuration.getMaxKeepAliveTime();
        this.minTestTime = configuration.getMinTestTime();
        
        Collection<OpenTsdbClientConfiguration> clientConfigs = configuration.getClientConfigurations();
        for (OpenTsdbClientConfiguration clientConfig : clientConfigs) {
            for ( int i = 0; i < clientConfig.getMaxConnections(); ++i) {
                SocketAddress address = new InetSocketAddress(clientConfig.getHost(), clientConfig.getPort());
                addresses.add(address);
            }
        }
    }

    /**
     * Create a new OpenTsdbClient using provided socket address
     * @return a new client with configured parameters
     * @throws IOException when SocketFactory#newSocket() throws
     */
    @Override
    public OpenTsdbClient makeObject() throws IOException {
        
        SocketAddress address;
        
        // Take the current address out and move it to the end of the queue
        synchronized (this) {
            address = addresses.remove();
            addresses.add(address);
        }
        
        // Build a new client
        Socket socket = socketFactory.newSocket(address);
        return new OpenTsdbClient(socket);
    }

    /**
     * <p>Check that the specified client is still OK to use</p>
     * <p>This checks multiple factors:
     * <ul>
     * <li>Has the client exceeded its maximum TTL?</li>
     * <li>Has the client been explicitly closed?</li>
     * <li>If more than the minimum test time has elapsed, can the client still 
     * receive data from the OpenTSDB server?</li>
     * </ul>
     * If the answer to any of these questions is "yes" the client will be
     * considered invalid and will not be used.
     * </p>
     * @param client
     * @return true if client is alive, false otherwise
     */
    @Override
    public boolean validateObject(OpenTsdbClient client) {
        //client exceeded his liveliness
        long now = System.currentTimeMillis();
        if ((now - client.getAllocated()) >= maxKeepAliveTime) {
            log.info("Client has exceeded its maximum lifetime and will be discarded.");
            return false;
        }
        //test client after specified interval
        else if ((now - client.getTested()) >= minTestTime) {
            if (client.isAlive()) {
                log.debug("Client tested out OK");
                client.updateTested();
            } 
            else {
                log.warn("Client not alive after min test interval: " + client.socketAddress());
                return false;
            }
        }
        
        return !client.isClosed(); // Has anyone explicitly closed the client?
    }

    /**
     * Closes the connection.
     * @param client 
     */
    @Override
    public void destroyObject(OpenTsdbClient client) {
        client.close();
    }
    
    private final SocketFactory socketFactory;
    private final Queue<SocketAddress> addresses;
    
    private final long maxKeepAliveTime;
    private final long minTestTime;
}
