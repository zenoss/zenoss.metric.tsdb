package org.zenoss.lib.tsdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.Map;


/**
 * OpenTsdbClient provides a socket interface to the OpenTsdb telnet service.  The client
 * provides #put to publish metrics.
 */
public class OpenTsdbClient {
    static final Logger log = LoggerFactory.getLogger(OpenTsdbClient.class);

    public OpenTsdbClient(OpenTsdbClientConfiguration configuration) {
        this.configuration = configuration;
    }

    /** is the socket connected */
    public boolean isConnected()  {
        return socket.isConnected();
    }

    /** is the socket closed */
    public boolean isClosed() {
        return socket.isClosed();
    }

    /**
     * Write a metric to the tsdb socket and read it's response
     */
    public void put(String bugger) throws IOException {
        out.write(bugger.getBytes());
    }

    public String version() throws IOException {
        out.write( "version\n".getBytes());
        out.flush();

        //TODO read...

        return "";
    }

    /** flush the output stream */
    public void flush() throws IOException {
        out.flush();
    }

    /**
     * Read one line from the tsdb socket
     */
    public String read() throws IOException {
        return reader.readLine();
    }

    /**
     * @throws IOException thrown by the underlying socket
     */
    public void open() throws IOException {
        log.info("Opening Tsdb Communication");
        socket = configuration.newSocket();
        in = socket.getInputStream();
        out = socket.getOutputStream();
        reader = new BufferedReader( new InputStreamReader( in));
    }

    /**
     * @throws IOException thrown by the underlying socket
     */
    public void close() {
        log.info("Closing Tsdb Communication");
        if (socket == null) {
            return;
        }
        try {
            socket.close();
        } catch (IOException ex) {
            log.error( "Exception closing socket:", ex);
        } finally {
            out = null;
            socket = null;
            reader = null;
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


    private Socket socket;

    private InputStream in;

    private OutputStream out;

    private BufferedReader reader;

    private OpenTsdbClientConfiguration configuration;
}
