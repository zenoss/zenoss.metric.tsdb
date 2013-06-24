package org.zenoss.metric.tsdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;


/**
 * OpenTsdbSocketClient provides a socket interface to the OpenTsdb telnet service.  The client
 * provides #put to publish metrics.
 */
public class OpenTsdbSocketClient {
    static final Logger log = LoggerFactory.getLogger(OpenTsdbSocketClient.class);

    public OpenTsdbSocketClient(String host, int port) {
        address = new InetSocketAddress(host, port);
    }

    /** Use the address provided to connect, use #open before interacting with the client */
    public OpenTsdbSocketClient(SocketAddress address) {
        this.address = address;
    }


    /** Open with existing socket. This ctor assumes the socket's already opened */
    public OpenTsdbSocketClient(Socket socket) throws IOException {
        this.socket = socket;
        this.address = socket.getRemoteSocketAddress();
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
    }

    /**
     * Write a metric to the tsdb socket and read it's response
     *
     * @throws IllegalStateException when not open
     */
    public synchronized void put(String bugger) throws IOException, OpenTsdbException {
        if (socket == null) {
            throw new IllegalStateException("Client not open");
        }
        log.debug("Writing {}:", bugger);
        out.write(bugger.getBytes());
        out.flush();

        //server appears to only respond on errors, yuck!, give it a few rounds to reply
        //check if there was an error...
        int available = 0;
        int round = 0;
        while (true) {
            if (++round > 5) {
                break;
            }
            available = in.available();

            //there was an error
            if (available > 0) {
                //give stream time to saturate
                do {
                    available = in.available();
                    sleep( 250);
                } while (available != in.available());
                available = in.available();
                byte[] response = new byte[available];
                in.read(response);
                String message = new String(response, "UTF-8");
                throw new OpenTsdbException(message);
            }

            //server EOF
            if (available < 0) {
                throw new OpenTsdbConnectionClosedException();
            }
            sleep( 50);
        }
    }

    /**
     * @throws IOException thrown by the underlying socket
     */
    public synchronized void open() throws IOException {
        log.info("Open Socket Communication: {}", address);
        socket = new Socket();
        socket.connect(address);
        socket.setKeepAlive(true);
        in = socket.getInputStream();
        out = socket.getOutputStream();
    }

    /**
     * @throws IOException thrown by the underlying socket
     */
    public synchronized void close() throws IOException {
        log.info("Closing Socket Communication: {}", address);
        if (socket == null) {
            return;
        }
        try {
            socket.close();
        } finally {
            in = null;
            out = null;
            socket = null;
        }
    }

    /**
     * convert a metric object into a metrics put byte buffer message
     */
    public static String toPutMessage(String name, long timestamp, double value, Map<String, String> tags) {
        StringBuilder builder = new StringBuilder();
        builder.append("put ");
        builder.append(name);
        builder.append(" ");
        builder.append(timestamp);
        builder.append(" ");
        builder.append(value);

        for (Map.Entry<String, String> entry : tags.entrySet()) {
            String entryName = entry.getKey();
            String entryValue = entry.getValue();
            builder.append(" ");
            builder.append(entryName);
            builder.append("=");
            builder.append(entryValue);
        }
        builder.append("\n");
        return builder.toString();
    }

    static void sleep( long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private SocketAddress address;
    private Socket socket;
    private InputStream in;
    private OutputStream out;
}
