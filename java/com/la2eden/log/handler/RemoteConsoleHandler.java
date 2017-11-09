package com.la2eden.log.handler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

public class RemoteConsoleHandler extends StreamHandler
{
    private static Boolean ENABLED;
    private static Boolean BLOCKING;
    private static Integer SERVER_PORT;
    private static String TOKEN;

    private static DataOutputStream stream;
    private static ServerSocket socket;
    private static Thread thread;

    private void configure() {
        LogManager manager = LogManager.getLogManager();
        String cname = getClass().getName();

        ENABLED = Boolean.valueOf(manager.getProperty(cname + ".enabled"));
        BLOCKING = Boolean.valueOf(manager.getProperty(cname + ".blocking"));
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

    public RemoteConsoleHandler() throws IOException {
        // We are going to use the logging defaults.
        configure();

        connect();
    }

    public RemoteConsoleHandler(int port) throws IOException {
        configure();
        SERVER_PORT = port;

        connect();
    }

    private void listen() {
        try {
            socket = new ServerSocket(SERVER_PORT);
            Socket client = null;
            Boolean connected = false;

            while (!connected) {
                client = socket.accept();

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

            stream = new DataOutputStream(client.getOutputStream());
            setOutputStream(stream);
        } catch (IOException e) {
            // code
        }
    }

    public void connect() {
        if (ENABLED) {
            Runnable listener = this::listen;

            if (BLOCKING) {
                listen();
            } else {
                thread = new Thread(listener);
                thread.start();
            }
        }
    }

    @Override
    public void publish(LogRecord record)
    {
        if (ENABLED && (stream != null)) {
            super.publish(record);
        }

        flush();
    }

    @Override
    public void flush() {
        super.flush();

        try {
            if (ENABLED && (stream != null)) {
                stream.flush();
            }
        } catch (IOException e) {
            // code
        }
    }

    @Override
    public void close() throws SecurityException {
        super.close();

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ix) {
                // drop through.
            }
        }

        socket = null;
    }
}
