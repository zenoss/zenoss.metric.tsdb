package org.zenoss.lib.tsdb;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class OpenTsdbClientConfiguration {
    @JsonProperty
    private String host;

    @JsonProperty
    private Integer port;

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    @SuppressWarnings({"unused"})
    public void setHost(String host) {
        this.host = host;
    }

    @SuppressWarnings({"unused"})
    public void setPort(Integer port) {
        this.port = port;
    }

    /** Open a socket using specified port and address */
    public Socket newSocket() throws IOException {
        SocketAddress address = new InetSocketAddress(getHost(), getPort());
        Socket socket = new Socket();
        socket.connect(address);
        socket.setKeepAlive(true);
        return socket;
    }
}
