package com.la2eden.log.handler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

public class SocketHandler extends StreamHandler
{
    private static Boolean ENABLED;
    private static Integer SERVER_PORT;
    private static String TOKEN;

    private static DataOutputStream OUT_STREAM;
    private static ServerSocket SOCKET;

    private void configure() {
        LogManager manager = LogManager.getLogManager();
        String cname = getClass().getName();

        ENABLED = Boolean.valueOf(manager.getProperty(cname + ".enabled"));
        SERVER_PORT = Integer.valueOf(manager.getProperty(cname + ".port"));
        TOKEN = manager.getProperty(cname + ".token");

        setLevel(Level.parse(manager.getProperty(cname + ".level")));
        try {
            setEncoding(manager.getProperty(cname +".encoding"));
        } catch (Exception ex) {
            try {
                setEncoding(null);
            } catch (Exception e) {
                // code
            }
        }
    }

    public SocketHandler() throws IOException {
        // We are going to use the logging defaults.
        configure();

        connect();
    }

    public SocketHandler(int port) throws IOException {
        configure();
        SERVER_PORT = port;

        connect();
    }

    public void connect() {
        if (ENABLED) {
            try {
                SOCKET = new ServerSocket(SERVER_PORT);
                Socket client = null;
                Boolean connected = false;

                while (!connected) {
                    client = SOCKET.accept();

                    if (client.isConnected()) {
                        connected = true;
                    }
                }

                InputStreamReader isr = new InputStreamReader(client.getInputStream());
                BufferedReader br = new BufferedReader(isr);
                String readTkn = br.readLine();
                String token;
                if (readTkn.startsWith("tkn:")) {
                    token = readTkn.replaceAll("tkn:", "");

                    if ((token == null) || (!token.equals(TOKEN))) {
                        close();
                        return;
                    }
                }

                OUT_STREAM = new DataOutputStream(client.getOutputStream());

                setOutputStream(OUT_STREAM);
            } catch (IOException e) {
                // code
            }
        }
    }

    @Override
    public void publish(LogRecord record)
    {
        if (!isLoggable(record)) {
            return;
        }

        if (ENABLED) {
            super.publish(record);
        }

        flush();
    }

    @Override
    public void flush() {
        super.flush();

        try {
            if (ENABLED) {
                OUT_STREAM.flush();
            }
        } catch (IOException e) {
            // code
        }
    }

    @Override
    public void close() throws SecurityException {
        super.close();
        if (SOCKET != null) {
            try {
                SOCKET.close();
            } catch (IOException ix) {
                // drop through.
            }
        }
        SOCKET = null;
    }
}
