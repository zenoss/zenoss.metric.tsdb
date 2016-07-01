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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import java.util.Comparator;
import java.util.LinkedList;


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
     * Wait for input on the tsdb socket. As soon as any becomes available, return all that became available.
     * @return All the input that became available, or null if the socket reached the end of its input.
     */
    public String read() throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader in = getInput();
        char[] buffer = new char[1024];
        int len = in.read(buffer);
        if (len <= 0)
            return null;
        else {
            sb.append(buffer, 0, len);
            while (in.ready()) {
                len = in.read(buffer);
                if (len > 0)
                    sb.append(buffer, 0, len);
            }
            return sb.toString();
        }
    }

    /** Flush the tsdb socket and see if any errors come back. */
    public List<String> checkForErrors() throws IOException {
        BufferedReader in = getInput();
        OutputStream out = getOutput();
        out.write("version\n".getBytes(charset));
        out.flush();
        boolean block = true;
        String line = null;
        List<String> errors = null;
        while (block || line != null) {
            if (block) {
                line = in.readLine();
                block = false;
            } else if (in.ready()) {
                line = in.readLine();
            } else {
                line = null;
            }
            if (line != null && !line.isEmpty() && !line.startsWith("net.opentsdb") && !line.startsWith("Built on ")) {
                if (errors == null) {
                    errors = new ArrayList<String>();
                }
                errors.add(line);
            }
        }
        return (errors == null) ? Collections.<String>emptyList() : errors;
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
            throw new IOException("no version response from server");
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
            return ver.startsWith( "net.opentsdb");
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

        // Sort the entries by key to guarantee their order.
        List<Map.Entry<String, String>> sortedTags = new LinkedList<>(tags.entrySet());
        Collections.sort(sortedTags, new Comparator<Map.Entry<String, String>>() {
            @Override
            public int compare(Map.Entry<String, String>a, Map.Entry<String, String>b) {
                return (a.getKey().compareTo(b.getKey()));
            }
        });

        for (Map.Entry<String, String> entry : sortedTags) {
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
    
    SocketAddress socketAddress() {
        return socket.getRemoteSocketAddress();
    }
    
    private OutputStream getOutput() throws IOException {
        if (output == null) {
            output = new BufferedOutputStream(socket.getOutputStream(), bufferSize);
        }
        return output;
    }

    private BufferedReader getInput() throws IOException {
        if (input == null) {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream(), charset));
        }
        return input;
    }
    
    // Dependencies
    private final Socket socket;
    
    // Internal state
    private OutputStream output;
    private BufferedReader input;
    private boolean closed;
    private final long allocated;
    
    // Configuration
    private final int bufferSize;
    private final Charset charset;
}
