package org.zenoss.lib.tsdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
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
    public OpenTsdbClient(Socket socket) {
        this.socket = socket;
        this.closed = false;
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
        socket.getOutputStream().write(bugger.getBytes());
    }

    /**
     * flush the output stream
     */
    public void flush() throws IOException {
        socket.getOutputStream().flush();
    }

    /**
     * Read one line from the tsdb socket
     */
    public String read() throws IOException {
        InputStream in = socket.getInputStream();

        //assuming a message size
        byte[] message = new byte[512];
        int read = in.read( message);

        if ( read <= 0) {
            return null;
        }

        return new String( message, 0, read);
    }

    /**
     * request version from socket server
     */
    public String version() throws IOException {
        OutputStream out = socket.getOutputStream();
        out.write("version\n".getBytes());
        out.flush();

        String version = read();
        if ( version == null) {
            throw new IOException( "no version response from server");
        }

        return version;
    }

    /**
     * test that the server is alive and available for communication
     * @return
     */
    public boolean isAlive() {
        try {
            String ver = version();
            return ver.startsWith( "net.opentsdb built at revision");
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

    private boolean closed;
    private final Socket socket;
}
