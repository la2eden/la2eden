package com.la2eden.log.handler;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

public class SocketHandler extends StreamHandler
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

    private void listen() {
        try {
            socket = new ServerSocket(SERVER_PORT);
            Socket client;

            while (true) {
                client = socket.accept();

                if (client.isConnected()) {
                    break;
                }
            }

            InputStreamReader isr = new InputStreamReader(client.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            String readTkn = br.readLine();
            String token;

            if (readTkn.startsWith("tkn:")) {
                token = readTkn.replaceAll("tkn:", "");

                if ((token != null) && (token.equals(TOKEN))) {
                    stream = new DataOutputStream(client.getOutputStream());
                    setOutputStream(stream);
                }
            }
        } catch (IOException e) {
            // code
        }
    }

    private void connect() {
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
                stream.flush();
            }
        } catch (IOException e) {
            // code
        }
    }

    /*
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
    */
}
