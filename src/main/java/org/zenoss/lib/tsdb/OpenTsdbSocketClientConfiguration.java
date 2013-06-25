package org.zenoss.lib.tsdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class OpenTsdbSocketClientConfiguration {
    static final Logger log = LoggerFactory.getLogger(OpenTsdbSocketClientConfiguration.class);

    @JsonProperty
    private String hostName;

    @JsonProperty
    private Integer port;

    public String getHostName() {
        return hostName;
    }

    public Integer getPort() {
        return port;
    }

    /** Open a socket using specified port and address */
    public Socket newSocket() throws IOException {
        log.info("Open Socket Communication: {}:{}", getHostName(), getPort());
        SocketAddress address = new InetSocketAddress(getHostName(), getPort());
        Socket socket = new Socket();
        socket.connect(address);
        socket.setKeepAlive(true);
        return socket;
    }
}
