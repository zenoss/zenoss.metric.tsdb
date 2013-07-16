package org.zenoss.lib.tsdb;

import java.io.IOException;
import java.net.*;

public class SocketFactory {

    public SocketFactory( SocketFactoryConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Create a new socket using parameters
     * @return a newly created socket that's ready to connect
     * @throws IOException thrown by underlying socket
     */
    public Socket newSocket() throws IOException {
        Socket socket = new Socket();
        socket.setKeepAlive( configuration.isKeepAlive());
        if ( configuration.getReceiveBufferSize() > 0) {
            socket.setReceiveBufferSize(configuration.getReceiveBufferSize());
        }

        if ( configuration.getSendBufferSize() > 0) {
            socket.setSendBufferSize( configuration.getSendBufferSize());
        }

        if ( configuration.getSoTimeout() > 0) {
            socket.setSoTimeout( configuration.getSoTimeout());
        }
        return socket;
    }

    /**
     * create a new socket and connect to provided address
     * @param address address to connect
     * @return a connected socket
     * @throws IOException when unable to connect within provided timeout
     */
    public Socket newSocket(SocketAddress address) throws IOException {
        Socket socket = newSocket();
        if ( configuration.getConnectTimeout() < 0) {
            socket.connect(address);
        } else {
            socket.connect(address, configuration.getConnectTimeout());
        }
        return socket;
    }


    private final SocketFactoryConfiguration configuration;
}
