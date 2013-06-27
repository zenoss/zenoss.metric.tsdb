package org.zenoss.lib.tsdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.zenoss.dropwizardspring.annotations.Managed;

import java.util.LinkedList;

@Managed
public class OpenTsdbClientPool implements com.yammer.dropwizard.lifecycle.Managed {
    static final Logger log = LoggerFactory.getLogger(OpenTsdbClientPool.class);

    public OpenTsdbClientPool() {
    }

    public OpenTsdbClientPool(OpenTsdbClientPoolConfiguration configuration, SocketFactory socketFactory) {
        this.configuration = configuration;
        this.socketFactory = socketFactory;
    }

    @Override
    public void start() throws Exception {
        boolean keepAlive = configuration.getKeepAlive();
        socketFactory.setKeepAlive(keepAlive);

        int sendBufferSize = configuration.getSendBufferSize();
        socketFactory.setSendBufferSize(sendBufferSize);

        int recvBufferSize = configuration.getReceiveBufferSize();
        socketFactory.setReceiveBufferSize(recvBufferSize);
    }

    @Override
    public void stop() throws Exception {
    }

    public OpenTsdbClient get() throws InterruptedException {
        int available = availableClients.size();
        while ( available > 0) {
            Client client = availableClients.removeFirst();
            //test connection
            //if connection success
            //unavailableClients.push(client);
            return client.client;
            //else {
            //client.close();
            //blacklistedClients.add( client);
            //int available = availableClients.size();
            //}
        }

        return null;
    }

    /** return client to available queue */
    public void put(OpenTsdbClient client) {
    }

    /** Are any clients alive and working */
    public boolean isAlive() {
        return !availableClients.isEmpty() && !unavailableClients.isEmpty();
    }

    /** connection pool configuration */
    @Autowired
    private OpenTsdbClientPoolConfiguration configuration;

    /** factory to create new client sockets */
    @Autowired
    private SocketFactory socketFactory;

    /** clients available for use */
    private LinkedList< Client> availableClients = new LinkedList<>();

    /** clients allocated and not available for use */
    private LinkedList< Client> unavailableClients = new LinkedList<>();

    /** clients who's server disconnected */
    private LinkedList< Client> deadClients = new LinkedList<>();

    /** client, configuration and runtime stats */
    private static class Client {
        long used;
        String host;
        Integer port;
        OpenTsdbClient client;
    };
}
