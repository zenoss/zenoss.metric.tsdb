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
     * create a new OpenTsdbClient using provided socket address;
     * @param address address to connect and create client with
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

    @Override
    public void destroyObject(OpenTsdbClient client) {
        client.close();
    }
    
    private final SocketFactory socketFactory;
    private final Queue<SocketAddress> addresses;
    
    private final long maxKeepAliveTime;
    private final long minTestTime;
}
