package org.zenoss.lib.tsdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;


/**
 * OpenTsdbClient provides a socket interface to the OpenTsdb telnet service.  The client
 * provides #put to publish metrics, a method to read the #version.  This class is not thread safe.
 * Furthermore, the socket's assumed to be opened before using this class.
 */
public class OpenTsdbClient {
    
    static final Logger log = LoggerFactory.getLogger(OpenTsdbClient.class);

    /**
     * @param socket use provided socket for connection
     */
    public OpenTsdbClient(Socket socket, int bufferSize) {
        this.socket = socket;
        this.closed = false;
        this.bufferSize = bufferSize;
        this.allocated = System.currentTimeMillis();
        this.charset = StandardCharsets.UTF_8; // Make configurable?
    }

    /**
     * is the socket closed
     */
    public boolean isClosed() {
        return closed || socket.isClosed();
    }

    /**
     * Write a metric to the tsdb socket and read it's response
     */
    public void put(String bugger) throws IOException {
        getOutput().write(bugger.getBytes(charset));
    }

    /**
     * flush the output stream
     */
    public void flush() throws IOException {
        getOutput().flush();
    }

    /**
     * Read one line from the tsdb socket
     */
    public String read() throws IOException {
        InputStream in = socket.getInputStream();

        //assuming a message size
        byte[] message = new byte[512];
        int read = in.read (message);

        if (read <= 0) {
            return null;
        }

        return new String (message, 0, read, charset);
    }

    /**
     * request version from socket server
     */
    public String version() throws IOException {
        OutputStream out = getOutput();
        out.write("version\n".getBytes(charset));
        out.flush();

        String version = read();
        if (version == null) {
            throw new IOException( "no version response from server");
        }

        return version;
    }

    /**
     * test that the server is alive and available for communication
     * @return true if data could be read, false otherwise
     */
    public boolean isAlive() {
        try {
            String ver = version();
            return ver.startsWith( "net.opentsdb ");
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * close the underlying socket
     */
    public void close() {
        log.debug("Closing Tsdb Communication");
        if (closed) {
            return;
        }
        try {
            socket.close();
        } catch (IOException ex) {
            log.error("Exception closing OpenTsdb socket:", ex);
        } finally {
            closed = true;
        }
    }

    /**
     * convert metric data into a put byte buffer message for
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
    
    long getAllocated() {
        return allocated;
    }
    
    long getTested() {
        return tested;
    }
    
    void updateTested() {
        tested = System.currentTimeMillis();
    }
    
    SocketAddress socketAddress() {
        return socket.getRemoteSocketAddress();
    }
    
    private OutputStream getOutput() throws IOException {
        if (output == null) {
            output = new BufferedOutputStream(socket.getOutputStream(), bufferSize);
        }
        return output;
    }
    
    // Dependencies
    private final Socket socket;
    
    // Internal state
    private OutputStream output;
    private boolean closed;
    private long tested;
    private final long allocated;
    
    // Configuration
    private final int bufferSize;
    private final Charset charset;
    

}
