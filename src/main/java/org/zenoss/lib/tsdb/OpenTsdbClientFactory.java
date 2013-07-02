package org.zenoss.lib.tsdb;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

/**
 *  A factory for creating OpenTsdbClients.
 */
public class OpenTsdbClientFactory {

    public OpenTsdbClientFactory(OpenTsdbClientFactoryConfiguration configuration) {
        this( new SocketFactory( configuration));
    }

    public OpenTsdbClientFactory(SocketFactory socketFactory) {
        this.socketFactory = socketFactory;
    }

    /**
     * create a new OpenTsdbClient using provided socket address;
     * @param address address to connect and create client with
     * @return a new client with configured parameters
     * @throws IOException when SocketFactory#newSocket() throws
     */
    public OpenTsdbClient newClient(SocketAddress address) throws IOException
    {
        Socket socket = socketFactory.newSocket(address);
        return new OpenTsdbClient( socket);
    }

    private final SocketFactory socketFactory;
}
