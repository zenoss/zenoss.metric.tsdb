package org.zenoss.lib.tsdb;

import java.io.IOException;
import java.net.*;

public class SocketFactory {

    public boolean getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public int getSendBufferSize() {
        return sendBufferSize;
    }

    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    /**
     * Create a new socket using parameters
     * @return a newly created socket that's ready to connect
     * @throws IOException thrown by underlying socket
     */
    public Socket newSocket() throws IOException {
        Socket socket = new Socket();
        socket.setKeepAlive( keepAlive);
        if ( receiveBufferSize <= 0) {
            socket.setReceiveBufferSize( receiveBufferSize);
        }

        if ( sendBufferSize <= 0) {
            socket.setSendBufferSize( sendBufferSize);
        }
        return socket;
    }

    /**
     * create a new socket and connect to provided host and
     * @param host hostname to connect
     * @param port port to connect
     * @param timeout timeout to connect
     * @return a connected socket
     * @throws IOException thrown by underlying socket
     * @throws SocketTimeoutException  when unable to connect within provided timeout
     */
    public Socket newSocket(String host, int port, int timeout) throws IOException {
        Socket socket = newSocket();
        SocketAddress address = new InetSocketAddress( host, port);
        socket.connect(address, timeout);
        return socket;
    }

    /** tcp keepalive setting */
    private boolean keepAlive;

    /** tcp send buffer size, set <= 0 to disable */
    private int sendBufferSize;

    /** tcp receive buffer size, set <= 0 to disable */
    private int receiveBufferSize;
}
