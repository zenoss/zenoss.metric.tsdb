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

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;


/**
 *  A factory for creating OpenTsdbClients.
 */
public class OpenTsdbClientFactory extends BasePoolableObjectFactory<OpenTsdbClient> {

    //  put: HBase error: 1000 RPCs waiting on "tsdb,,1398325180794.54ad8182f2f2a0a1cc6d39ba26ca7f64." to come back online
    private static final Pattern COLLISION_ERROR_PATTERN = Pattern.compile(".*HBase error: .* RPCs waiting on .* to come back online");

    public OpenTsdbClientFactory(OpenTsdbClientPoolConfiguration configuration) {
        this(configuration, new SocketFactory (configuration.getClientFactoryConfiguration()));
    }
    
    public OpenTsdbClientFactory(OpenTsdbClientPoolConfiguration configuration, SocketFactory socketFactory)
    {
        this.socketFactory = socketFactory;
        this.addresses = new LinkedList<>();
        this.errorCount = new AtomicInteger();
        this.collision = new AtomicBoolean(false);
        
        this.maxKeepAliveTime = configuration.getMaxKeepAliveTime();
        this.clientBufferSize = configuration.getClientBufferSize();
        
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
        return new OpenTsdbClient(socket, clientBufferSize);
    }

    /**
     * <p>Check that the specified client is still OK to use</p>
     * <p>This checks multiple factors:
     * <ul>
     * <li>Has the client exceeded its maximum TTL?</li>
     * <li>Has the client or its socket been closed?</li>
     * <li>Ping the OpenTSDB server. Any exceptions or error messages sent in response?</li>
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

        if (client.isClosed())
            return false;

        try {
            boolean anyErrors = false;
            for (String error : client.checkForErrors()) {
                anyErrors = true;
                log.warn("Client returned error: {}", error);
                errorCount.incrementAndGet();
                if (COLLISION_ERROR_PATTERN.matcher(error).matches()) {
                    collision.set(true);
                }
            }
            if (anyErrors)
                return false;
        } catch (IOException e) {
            log.warn("Caught IOException checking for errors", e);
            errorCount.incrementAndGet();
            return false;
        }
        log.debug("Client tested out OK");

        return true;
    }

    /**
     * Closes the connection.
     * @param client 
     */
    @Override
    public void destroyObject(OpenTsdbClient client) {
        client.close();
    }

    public int clearErrorCount() {
        return errorCount.getAndSet(0);
    }

    /**
     * @return boolean
     */
    public boolean hasCollision() {
        return collision.getAndSet(false);
    }

    public void clearCollision() {
        collision.set(false);
    }

    static final Logger log = LoggerFactory.getLogger(OpenTsdbClientFactory.class);
    
    private final SocketFactory socketFactory;
    private final Queue<SocketAddress> addresses;
    
    private final long maxKeepAliveTime;
    private final int clientBufferSize;
    
    private final AtomicInteger errorCount;
    private final AtomicBoolean collision;
}
