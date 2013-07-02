package org.zenoss.lib.tsdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;

public class OpenTsdbClientPool {
    static final Logger log = LoggerFactory.getLogger(OpenTsdbClientPool.class);

    public OpenTsdbClientPool(OpenTsdbClientPoolConfiguration configuration) {
        this( configuration, new OpenTsdbClientFactory( configuration.getClientFactoryConfiguration()));
    }

    public OpenTsdbClientPool(OpenTsdbClientPoolConfiguration configuration, OpenTsdbClientFactory clientFactory) {
        this.configuration = configuration;
        this.clientFactory = clientFactory;

        Collection<OpenTsdbClientConfiguration> clientConfigs = configuration.getClientConfigurations();
        for (OpenTsdbClientConfiguration clientConfig : clientConfigs) {
            SocketAddress address = new InetSocketAddress(clientConfig.getHost(), clientConfig.getPort());
            add(address);
        }
    }

    /**
     * block until there's an available client
     */
    public synchronized OpenTsdbClient get() throws InterruptedException {
        int attempt = 0;
        MetaData data = null;
        while (true) {
            data = nextAvailable();
            if (data != null) {
                break;
            }
            data = revive();
            if (data != null) {
                break;
            }
            wait( backoff( attempt++));
        }

        return data.client;
    }

    /**
     * return client to available pool
     */
    public synchronized void put(OpenTsdbClient client) {
        if (client == null) {
            return;
        }
        MetaData data = lookup(client);
        if (data == null) {
            log.warn("Pool does not manage client: {}", client);
            return;
        }
        available.addFirst(data.pos);
        notify();
    }

    /**
     * close client and return to dead pool
     */
    public synchronized void kill(OpenTsdbClient client) {
        if (client == null) {
            return;
        }
        MetaData data = lookup(client);
        if (data == null) {
            log.warn("Pool does not manage client: {}", client);
            return;
        }
        data.client.close();
        data.client = null;
        dead.addLast(data.pos);
    }

    /**
     * add a socket to the available socket addresses
     */
    public synchronized void add(SocketAddress address) {
        int i = clients.size();
        MetaData data = new MetaData(i, address);
        clients.add(data);
        dead.addFirst(i);
        notify();
    }

    /**
     * iterate through all client meta-data configs looking for specific client
     */
    private MetaData lookup(OpenTsdbClient client) {
        for (MetaData data : clients) {
            if (data.client == client) {
                return data;
            }
        }
        return null;
    }

    /**
     * find the next available client, close old clients, and test connectivity (LIFO)
     */
    private MetaData nextAvailable() {
        MetaData data = null;
        while (available.size() > 0 && data == null) {
            int position = available.removeFirst();
            data = clients.get(position);

            //client exceeded his liveliness
            long now = System.currentTimeMillis();
            if ((now - data.allocated) >= configuration.getMaxKeepAliveTime()) {
                dead.addLast(data.pos);
                data.client.close();
                data.client = null;
                data = null;
            }
            //test client after specified interval
            else if ((now - data.tested) >= configuration.getMinTestTime()) {
                if (data.client.isAlive()) {
                    data.tested = now;
                } else {
                    log.warn("Client not alive after min test interval: " + data.address);
                    dead.addLast(data.pos);
                    data.client.close();
                    data.client = null;
                    data = null;
                }
            }
        }

        return data;
    }

    /**
     * take a client off the dead list and attempt to connect (FIFO)
     */
    private MetaData revive() {
        MetaData data = null;
        if (dead.size() > 0) {
            int position = dead.removeFirst();
            data = clients.get(position);
            try {
                data.client = clientFactory.newClient(data.address);
                data.allocated = System.currentTimeMillis();
                if (data.client.isAlive()) {
                    data.tested = data.allocated;
                } else {
                    log.warn("Client not alive after creation: " + data.address);
                    data.client.close();
                    data.client = null;
                    data = null;
                }
            } catch (IOException ex) {
                log.error("Failed to create client: " + data.address, ex);
                dead.addLast(position);
            }
        }
        return data;
    }


    /** backoff sleep timeout */
    public long backoff( int attempt) {
        if ( attempt > 10) {
            attempt = 10;
        }
        return (long) (1000l * ((1 << attempt) / 2.0));
    }

    /**
     * clients available for allocation
     */
    private final LinkedList<Integer> available = new LinkedList<>();

    /**
     * clients currently killed
     */
    private final LinkedList<Integer> dead = new LinkedList<>();

    /**
     * client tracking data
     */
    private final ArrayList<MetaData> clients = new ArrayList<>();

    /**
     * configuration parameters
     */
    private final OpenTsdbClientPoolConfiguration configuration;

    /**
     * factory to create new client sockets
     */
    private final OpenTsdbClientFactory clientFactory;

    /**
     * client, configuration and runtime pooling stats
     */
    private static class MetaData {
        int pos;
        long tested;
        long allocated;
        SocketAddress address;
        OpenTsdbClient client;

        MetaData(int pos, SocketAddress address) {
            this.pos = pos;
            this.address = address;
        }
    }
}
