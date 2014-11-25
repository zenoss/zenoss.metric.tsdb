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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration details for socket connections to OpenTSDB
 */
public class SocketFactoryConfiguration {

    /** tcp keepalive setting */
    @JsonProperty
    private boolean keepAlive;

    /** tcp send buffer size, set <= 0 to disable */
    @JsonProperty
    private int sendBufferSize;

    /** tcp receive buffer size, set <= 0 to disable */
    @JsonProperty
    private int receiveBufferSize;

    /** tcp connect timeout, set <= 0 to disable and use defaults */
    @JsonProperty
    private int connectTimeout;

    /**  socket read timeout, set <= 0 to use defaults */
    @JsonProperty
    private int soTimeout;

    /**
     * The maximum time to wait when establishing a socket connection
     * @return time in milliseconds
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }
    
    /**
     * The buffer size used for input from sockets
     * @return buffer size
     */
    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }
    
    /**
     * The buffer size used for output to sockets
     * @return buffer size
     */
    public int getSendBufferSize() {
        return sendBufferSize;
    }

    /**
     * The maximum time to wait when reading from a socket
     * @return time in milliseconds
     */
    public int getSoTimeout() {
        return soTimeout;
    }
    
    /**
     * Whether or not to have socket keep alive turned on
     * @return keepAlive
     */
    public boolean isKeepAlive() {
        return keepAlive;
    }

    /**
     * The maximum time to wait when establishing a socket connection
     * @param connectTimeout time in milliseconds
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * Whether or not to have socket keep alive turned on
     * @param keepAlive keepAlive
     */
    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    /**
     * The buffer size used for input from sockets
     * @param receiveBufferSize buffer size
     */
    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    /**
     * The buffer size used for output to sockets
     * @param sendBufferSize buffer size
     */
    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }
    
    /**
     * The maximum time to wait when reading from a socket
     * @param soTimeout time in milliseconds
     */
    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }
}
